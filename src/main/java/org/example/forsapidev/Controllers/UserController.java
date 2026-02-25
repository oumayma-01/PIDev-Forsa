package org.example.forsapidev.Controllers;


//import  com.digidtoolsbackend.Firstmodels.Collaborateur;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.example.forsapidev.Services.Interfaces.IUserService;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
  @Autowired
  IUserService iUserService;

@SecurityRequirement(name = "Bearer Authentication")
@GetMapping("/all")
  public List<User> all() {
    return iUserService.findall();
  }
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/find/{id}")
  public UserResponse find(@PathVariable("id") long id)
  {
    UserResponse u =iUserService.findbyId(id);
    return u;
  }
  @SecurityRequirement(name = "Bearer Authentication")
  @DeleteMapping("/delete/{id}")
  public String delete(@PathVariable("id") long id ) {
    iUserService.delete(id);
    return "Deleted Successfully";
  }
  @SecurityRequirement(name = "Bearer Authentication")
  @PutMapping("/update/{id}")
  public ResponseEntity<?> UpdateUser(@Valid @RequestBody SignupRequest signUpRequest, @PathVariable("id") long id) {
return iUserService.UpdateUser(signUpRequest,id);
  }
  @SecurityRequirement(name = "Bearer Authentication")
  @PutMapping("/updateP")
  public ResponseEntity<?> UpdatePassword(@RequestParam Long id, @RequestParam String password) {
return iUserService.updatePassword(id,password);
  }
}
