package org.example.forsapidev.Services.Implementation;


import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IAuthService;
import org.example.forsapidev.Utils.EmailEncryptionUtil;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.ForgottenPasswordRequest;
import org.example.forsapidev.payload.request.LoginRequest;
import org.example.forsapidev.payload.request.ResetRequest;
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
import java.util.Optional;
import java.util.UUID;
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
    EmailSenderService emailSenderService;
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
        user.setIsActive(false);
        user.setCreatedAt(new Date());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        Role role = roleRepository.findById(signUpRequest.getIdrole())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRole(role);
        userRepository.save(user);
        String request = "http://localhost:4200/pages/validateUser/";
        String subject ="Verification of your email";
        String imagePath = "classpath:static/uploads/logoforsa.png";
        String appUrl = request + user.getId();
        String body = " <html> <head> <meta charset='UTF-8'> <title>Email Verification</title> <style>"
                + """
          * {
                   margin: 0;
                   padding: 0;
                   box-sizing: border-box;
                   font-family: Arial, Helvetica, sans-serif;
                 }
                 .container {
                   max-width: 600px;
                   margin: auto;
                   padding: 20px;
                   border: 1px solid #ccc;
                   border-radius: 5px;
                   background-color: #f4f4f4;
                 }
                 h1 {
                   margin-bottom: 20px;
                   text-align: center;
                   color: #444;
                 }
                 img {
                   max-width: 100%;
                   height: auto;
                   margin: auto;
                   margin-bottom: 20px;
                   display: block;
                 }
                 p {
                   margin-bottom: 10px;
                   line-height: 1.5;
                   color: #444;
                 }
                 a {
                   color: #fff !important;
                   text-decoration: none;
                 }
                 a:hover {
                   text-decoration: underline;
                 }
                 .imgI {
                   max-width: 100%;
                   height: auto;
                   width: 50%;
                   display: block;
                   margin: 10% auto;
                 }
                 .cta-btn {
                   display: block;
                   width: 100%;
                   max-width: 200px;
                   margin: 20px auto;
                   padding: 10px;
                   border-radius: 15px;
                   background-color: #8095F2;
                   color: #fff;
                   text-align: center;
                   text-decoration: none;
                 }
                 .signature {
                   margin-top: 20px;
                   text-align: left;
                   font-size: 12px;
                   color: #888;
                 }
               </style>
         """
                +
                " </head> <body> <div class='container'> <img src='cid:image' class='imgI' width='100%'>"
                + " <h1>Email Verification</h1>"
                + "<p>Thank you for registering with Forsa. You are receiving this email to verify your account's email address.</p>"
                + " <p>Please click the link below or copy and paste it into your browser to complete the verification process:</p>"
                + " <a href='" + appUrl + "' class='cta-btn'>Verify Email</a>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailSenderService.sendEmailWithImage(signUpRequest.getEmail(),subject, body,imagePath);
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
    @Override
    public ResponseEntity<?> ForgottenPassword(ForgottenPasswordRequest loginRequest) throws Exception {

        Optional<User> optional = userRepository.findByEmail(loginRequest.getEmail());
        String request = "http://localhost:4200/pages/changepassword/";

        if (optional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("This email address is not valid"));
        }

        User user = optional.get();

        // Generate reset token
        user.setResetToken(UUID.randomUUID().toString());
        user.setExpiryDate(30); // 30 minutes validity

        userRepository.save(user);

        String subject = "Password Reset Request";
        String imagePath = "classpath:static/uploads/digid.png";

        String appUrl = request
                + user.getResetToken()
                + "/"
                + EmailEncryptionUtil.encryptEmail(user.getEmail()).toString();

        String body = " <html> <head> <meta charset='UTF-8'> <title>Reset Password</title> <style>"
                + """
              * {
                       margin: 0;
                       padding: 0;
                       box-sizing: border-box;
                       font-family: Arial, Helvetica, sans-serif;
                     }
                     .container {
                       max-width: 600px;
                       margin: auto;
                       padding: 20px;
                       border: 1px solid #ccc;
                       border-radius: 5px;
                       background-color: #f4f4f4;
                     }
                     h1 {
                       margin-bottom: 20px;
                       text-align: center;
                       color: #444;
                     }
                     img {
                       max-width: 100%;
                       height: auto;
                       margin: auto;
                       margin-bottom: 20px;
                       display: block;
                     }
                     p {
                       margin-bottom: 10px;
                       line-height: 1.5;
                       color: #444;
                     }
                     a {
                       color: #fff !important;
                       text-decoration: none;
                     }
                     a:hover {
                       text-decoration: underline;
                     }
                     .imgI {
                       max-width: 100%;
                       height: auto;
                       width: 50%;
                       display: block;
                       margin: 10% auto;
                     }
                     .cta-btn {
                       display: block;
                       width: 100%;
                       max-width: 200px;
                       margin: 20px auto;
                       padding: 10px;
                       border-radius: 15px;
                       background-color: #8095F2;
                       color: #fff;
                       text-align: center;
                       text-decoration: none;
                     }
                     .signature {
                       margin-top: 20px;
                       text-align: left;
                       font-size: 12px;
                       color: #888;
                     }
                   </style>
             """
                +
                " </head> <body> <div class='container'> <img src='cid:image' class='imgI' width='100%'>"
                + " <h1>Reset Your Password</h1>"
                + "<p>You are receiving this email because you (or someone else) requested a password reset for your account.</p>"
                + " <p>Please click the link below or copy and paste it into your browser to complete the process:</p>"
                + " <a href='" + appUrl + "' class='cta-btn'>Reset Password</a>"
                + "<p>If you did not request this, please ignore this email and your password will remain unchanged.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailSenderService.sendEmailWithImage(
                loginRequest.getEmail(),
                subject,
                body,
                imagePath
        );

        return ResponseEntity.ok(
                new MessageResponse("Password reset link has been sent to your email!")
        );
    }
    @Override
    public ResponseEntity<?> resetpass(ResetRequest restRequest) throws Exception {
String email = EmailEncryptionUtil.decryptEmail(restRequest.getEmail());
        Optional<User> optional = userRepository.findByEmail(email);

        if (optional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid email"));
        }

        User user = optional.get();

        if (user.getResetToken() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Reset token not found"));
        }

        if (!user.getResetToken().equals(restRequest.getToken())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid reset token"));
        }

        if (user.isExpired()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Reset token has expired. Please request a new one."));
        }

        if (restRequest.getPassword() == null || restRequest.getPassword().length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Password must be between 6 and 40 characters"));
        }

        // Update password
        user.setPasswordHash(encoder.encode(restRequest.getPassword()));

        // Invalidate token after use
        user.setResetToken(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                new MessageResponse("Password changed successfully")
        );
    }
}
