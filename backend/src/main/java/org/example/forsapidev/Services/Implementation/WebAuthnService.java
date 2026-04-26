package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Repositories.WebAuthnCredentialRepository;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.entities.UserManagement.WebAuthnCredential;
import org.example.forsapidev.payload.request.WebAuthnBeginLoginRequest;
import org.example.forsapidev.payload.request.WebAuthnBeginRegisterRequest;
import org.example.forsapidev.payload.request.WebAuthnFinishLoginRequest;
import org.example.forsapidev.payload.request.WebAuthnFinishRegisterRequest;
import org.example.forsapidev.payload.response.JwtResponse;
import org.example.forsapidev.payload.response.WebAuthnBeginLoginResponse;
import org.example.forsapidev.payload.response.WebAuthnBeginRegisterResponse;
import org.example.forsapidev.payload.response.WebAuthnCredentialResponse;
import org.example.forsapidev.security.jwt.JwtUtils;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebAuthnService {

    private record PendingChallenge(String challenge, long expiresAtEpochMs, Long userId, String username) {
    }

    private final UserRepository userRepository;
    private final WebAuthnCredentialRepository credentialRepository;
    private final JwtUtils jwtUtils;
    private final IRoleAccessService roleAccessService;
    private final Map<String, PendingChallenge> registerChallenges = new ConcurrentHashMap<>();
    private final Map<String, PendingChallenge> loginChallenges = new ConcurrentHashMap<>();
    private final Map<String, ArrayDeque<Long>> rateLimit = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final String rpId;
    private final String rpName;

    public WebAuthnService(
            UserRepository userRepository,
            WebAuthnCredentialRepository credentialRepository,
            JwtUtils jwtUtils,
            IRoleAccessService roleAccessService,
            @Value("${app.webauthn.rp-id:localhost}") String rpId,
            @Value("${app.webauthn.rp-name:Forsa}") String rpName) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.jwtUtils = jwtUtils;
        this.roleAccessService = roleAccessService;
        this.rpId = rpId;
        this.rpName = rpName;
    }

    public ResponseEntity<?> beginRegister(Authentication authentication, WebAuthnBeginRegisterRequest request) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        if (!allow("reg:" + principal.getUsername(), 6, 60_000L)) {
            return ResponseEntity.status(429).body(Map.of("message", "Too many registration attempts"));
        }
        String challenge = randomB64Url(32);
        registerChallenges.put(principal.getUsername(),
                new PendingChallenge(challenge, System.currentTimeMillis() + 120_000L, principal.getId(), principal.getUsername()));

        List<String> exclude = credentialRepository.findByUserIdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(WebAuthnCredential::getCredentialId)
                .toList();

        return ResponseEntity.ok(new WebAuthnBeginRegisterResponse(
                challenge,
                rpId,
                rpName,
                toBase64Url(String.valueOf(principal.getId())),
                principal.getUsername(),
                principal.getUsername(),
                exclude,
                60_000L,
                "platform",
                "preferred",
                "preferred"));
    }

    public ResponseEntity<?> finishRegister(Authentication authentication, WebAuthnFinishRegisterRequest request) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        PendingChallenge pending = registerChallenges.get(principal.getUsername());
        if (pending == null || pending.expiresAtEpochMs < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration challenge expired"));
        }
        if (request == null || isBlank(request.getCredentialId()) || isBlank(request.getClientDataJSON())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid registration payload"));
        }
        Optional<WebAuthnCredential> existing = credentialRepository.findByCredentialId(request.getCredentialId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Credential already exists"));
        }
        Long principalUserId = principal.getId();
        if (principalUserId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        User user = userRepository.findById(principalUserId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        WebAuthnCredential credential = new WebAuthnCredential();
        credential.setUser(user);
        credential.setCredentialId(request.getCredentialId());
        credential.setPublicKey(isBlank(request.getPublicKey()) ? request.getAttestationObject() : request.getPublicKey());
        credential.setTransports(request.getTransports());
        credential.setDeviceName(isBlank(request.getDeviceName()) ? "Current device" : request.getDeviceName().trim());
        credential.setSignCount(0L);
        credentialRepository.save(credential);
        registerChallenges.remove(principal.getUsername());
        return ResponseEntity.ok(Map.of("message", "Passkey registered successfully"));
    }

    public ResponseEntity<?> beginLogin(WebAuthnBeginLoginRequest request) {
        String username = request != null ? nullSafeTrim(request.getUsername()) : "";
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
        }
        if (!allow("login:" + username, 10, 60_000L)) {
            return ResponseEntity.status(429).body(Map.of("message", "Too many authentication attempts"));
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not eligible for passkey login"));
        }
        List<String> allowCredentials = credentialRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(WebAuthnCredential::getCredentialId)
                .toList();
        if (allowCredentials.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No passkeys registered for this account"));
        }
        String challenge = randomB64Url(32);
        loginChallenges.put(username, new PendingChallenge(challenge, System.currentTimeMillis() + 120_000L, user.getId(), username));
        return ResponseEntity.ok(new WebAuthnBeginLoginResponse(
                challenge,
                rpId,
                allowCredentials,
                60_000L,
                "preferred"));
    }

    public ResponseEntity<?> finishLogin(WebAuthnFinishLoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getCredentialId()) || isBlank(request.getClientDataJSON())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid authentication payload"));
        }
        String username = request.getUsername().trim();
        PendingChallenge pending = loginChallenges.get(username);
        if (pending == null || pending.expiresAtEpochMs < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Authentication challenge expired"));
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not eligible for passkey login"));
        }
        WebAuthnCredential credential = credentialRepository.findByCredentialId(request.getCredentialId()).orElse(null);
        if (credential == null || credential.getUser() == null || !credential.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unknown credential for this user"));
        }
        credential.setLastUsedAt(Date.from(Instant.now()));
        long currentCount = credential.getSignCount() == null ? 0L : credential.getSignCount();
        credential.setSignCount(currentCount + 1);
        credentialRepository.save(credential);
        loginChallenges.remove(username);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                UserDetailsImpl.build(user),
                null,
                UserDetailsImpl.build(user).getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtils.generateJwtForUserId(user.getId());
        List<String> roles = UserDetailsImpl.build(user).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
        jwtResponse.setHasProfileImage(user.getProfileImageKey() != null && !user.getProfileImageKey().isBlank());
        jwtResponse.setOauthAccount("GOOGLE".equalsIgnoreCase(user.getAuthProvider()));
        jwtResponse.setAllowedNavPaths(roleAccessService.permittedNavPathsForRole(user.getRole().getName()));
        return ResponseEntity.ok(jwtResponse);
    }

    public ResponseEntity<?> listCredentials(Authentication authentication) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        List<WebAuthnCredentialResponse> data = credentialRepository.findByUserIdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(c -> new WebAuthnCredentialResponse(
                        c.getCredentialId(),
                        c.getDeviceName(),
                        c.getTransports(),
                        c.getCreatedAt(),
                        c.getLastUsedAt()))
                .toList();
        return ResponseEntity.ok(data);
    }

    public ResponseEntity<?> deleteCredential(Authentication authentication, String credentialId) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        WebAuthnCredential credential = credentialRepository.findByCredentialId(credentialId).orElse(null);
        if (credential == null || credential.getUser() == null || !credential.getUser().getId().equals(principal.getId())) {
            return ResponseEntity.status(404).body(Map.of("message", "Credential not found"));
        }
        credentialRepository.delete(credential);
        return ResponseEntity.ok(Map.of("message", "Passkey removed"));
    }

    private boolean allow(String key, int max, long windowMs) {
        long now = System.currentTimeMillis();
        ArrayDeque<Long> hits = rateLimit.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && now - hits.peekFirst() > windowMs) {
                hits.pollFirst();
            }
            if (hits.size() >= max) {
                return false;
            }
            hits.addLast(now);
            return true;
        }
    }

    private String randomB64Url(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String toBase64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
    }

    private static String nullSafeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
