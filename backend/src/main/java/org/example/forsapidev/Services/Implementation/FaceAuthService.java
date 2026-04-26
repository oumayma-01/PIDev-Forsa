package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.Repositories.FaceBiometricProfileRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.FaceBiometricProfile;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.FaceEnrollRequest;
import org.example.forsapidev.payload.request.FaceLoginRequest;
import org.example.forsapidev.payload.response.JwtResponse;
import org.example.forsapidev.payload.response.MessageResponse;
import org.example.forsapidev.security.jwt.JwtUtils;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaceAuthService {
    private static final int MIN_VECTOR_SIZE = 64;
    private final FaceBiometricProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final IRoleAccessService roleAccessService;
    private final ObjectMapper objectMapper;
    private final double minSimilarity;

    public FaceAuthService(
            FaceBiometricProfileRepository profileRepository,
            UserRepository userRepository,
            JwtUtils jwtUtils,
            IRoleAccessService roleAccessService,
            ObjectMapper objectMapper,
            @Value("${app.face-auth.min-similarity:0.88}") double minSimilarity) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.roleAccessService = roleAccessService;
        this.objectMapper = objectMapper;
        this.minSimilarity = minSimilarity;
    }

    public ResponseEntity<?> enroll(Authentication authentication, FaceEnrollRequest request) {
        if (!(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }
        if (request == null || request.getDescriptor() == null || request.getDescriptor().size() < MIN_VECTOR_SIZE) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid face descriptor"));
        }
        Long principalUserId = principal.getId();
        if (principalUserId == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }
        User user = userRepository.findById(principalUserId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(new MessageResponse("User not found"));
        }
        FaceBiometricProfile profile = profileRepository.findByUserId(user.getId()).orElseGet(FaceBiometricProfile::new);
        profile.setUser(user);
        try {
            profile.setDescriptorJson(objectMapper.writeValueAsString(request.getDescriptor()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Could not serialize face descriptor"));
        }
        profileRepository.save(profile);
        return ResponseEntity.ok(new MessageResponse("Face enrolled successfully"));
    }

    public ResponseEntity<?> login(FaceLoginRequest request) {
        if (request == null || request.getDescriptor() == null || request.getDescriptor().size() < MIN_VECTOR_SIZE) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid face descriptor"));
        }
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Username is required"));
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Unknown username"));
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Your account is deactivated"));
        }
        Optional<FaceBiometricProfile> profileOpt = profileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("No face profile enrolled for this account"));
        }
        List<Double> stored;
        try {
            stored = objectMapper.readValue(profileOpt.get().getDescriptorJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Stored face profile is corrupted"));
        }
        double similarity = cosineSimilarity(stored, request.getDescriptor());
        if (Double.isNaN(similarity) || similarity < minSimilarity) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Face identity mismatch",
                    "similarity", similarity));
        }

        UserDetailsImpl details = UserDetailsImpl.build(user);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        details, null, details.getAuthorities()));

        String jwt = jwtUtils.generateJwtForUserId(user.getId());
        List<String> roles = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        JwtResponse response = new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
        response.setHasProfileImage(user.getProfileImageKey() != null && !user.getProfileImageKey().isBlank());
        response.setOauthAccount("GOOGLE".equalsIgnoreCase(user.getAuthProvider()));
        response.setAllowedNavPaths(roleAccessService.permittedNavPathsForRole(user.getRole().getName()));
        return ResponseEntity.ok(response);
    }

    private static double cosineSimilarity(List<Double> a, List<Double> b) {
        int n = Math.min(a.size(), b.size());
        if (n == 0) return Double.NaN;
        double dot = 0d;
        double na = 0d;
        double nb = 0d;
        for (int i = 0; i < n; i += 1) {
            double va = safe(a.get(i));
            double vb = safe(b.get(i));
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na == 0d || nb == 0d) return Double.NaN;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static double safe(Double value) {
        return value == null ? 0d : value;
    }
}
