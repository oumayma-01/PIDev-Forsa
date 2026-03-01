package org.example.forsapidev.Controllers;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.forsapidev.Services.Interfaces.IAuthService;
import org.example.forsapidev.payload.request.ForgottenPasswordRequest;
import org.example.forsapidev.payload.request.LoginRequest;
import org.example.forsapidev.payload.request.ResetRequest;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {


  @Autowired
  private IAuthService iAuthService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return iAuthService.authenticateUser(loginRequest);
  }
  @GetMapping("/google-login-url")
  public ResponseEntity<?> getGoogleLoginUrl(HttpServletRequest request) {
    String baseUrl = "http://localhost:8088/forsaPidev";
    String googleAuthUrl = baseUrl + "/oauth2/authorization/google";
    return ResponseEntity.ok(new MessageResponse(googleAuthUrl));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signUpRequest) {
    return iAuthService.register(signUpRequest);
  }
  @PutMapping("/activate/{id}")
  public ResponseEntity<?> ValidateUser(@PathVariable("id") long id) {
    return iAuthService.ValidateUser(id);
  }
  @PostMapping("/ForgottenPassword")
  public ResponseEntity<?> ForgottenPassword(@Valid @RequestBody ForgottenPasswordRequest forgottenPasswordRequest) throws Exception {
    return iAuthService.ForgottenPassword(forgottenPasswordRequest);
  }

  @PostMapping("/resetpass")
  public ResponseEntity<?> resetpass(@RequestBody ResetRequest resetRequest) throws Exception {
    return iAuthService.resetpass(resetRequest);
  }
}
