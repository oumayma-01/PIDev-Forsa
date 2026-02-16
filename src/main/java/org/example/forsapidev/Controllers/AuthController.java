package org.example.forsapidev.Controllers;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IAuthService;
import org.example.forsapidev.payload.request.LoginRequest;
import org.example.forsapidev.payload.request.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {


  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  private IAuthService iAuthService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return iAuthService.authenticateUser(loginRequest);
  }


  @PostMapping("/signup")
  public ResponseEntity<?> registerCollaborator(@Valid @RequestBody SignupRequest signUpRequest) {
    return iAuthService.register(signUpRequest);
  }
  @SecurityRequirement(name = "Bearer Authentication")
  @PutMapping("/activate/{id}")
  public ResponseEntity<?> ValidateUser(@PathVariable("id") long id) {
    return iAuthService.ValidateUser(id);
  }

}
