package org.example.forsapidev.Services.Implementation;




import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IAuthService;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.LoginRequest;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.JwtResponse;
import org.example.forsapidev.payload.response.MessageResponse;
import org.example.forsapidev.security.jwt.JwtUtils;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
class AuthService implements IAuthService {
    @Autowired

    UserRepository userRepository;

    @Autowired

    RoleRepository roleRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder encoder;
    @Override
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        if (userRepository.existsByUsername(loginRequest.getUsername())) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
                User user = userRepository.findByUsername(loginRequest.getUsername()).get();
                if (user.getIsActive().equals(false)){
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Your account has been deactivated. Please contact the administrator for more information."));
            }
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
            } catch (Exception e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("The username or password is incorrect. Please verify the entered fields"));
            }
        }else return ResponseEntity
                .badRequest()
                .body(new MessageResponse("The username could not be found"));
    }
    @Override
    public ResponseEntity<?> register(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setIsActive(true);
        user.setCreatedAt(new Date());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        Role role = roleRepository.findById(signUpRequest.getIdrole())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User created Successfully"));
    }

    @Override
    public ResponseEntity<?> ValidateUser(long iduser) {
        User user = userRepository.findById(iduser).orElseThrow(() -> new RuntimeException("Error: User is not found."));
        if(user.getIsActive().equals(true)){
            return ResponseEntity
                    .ok(new MessageResponse("Your email address is already verified."));
        }
        user.setIsActive(true);
        userRepository.save(user);
        return ResponseEntity
                .ok(new MessageResponse("Your email address has been successfully verified. You can now access your account"));
    }


}
