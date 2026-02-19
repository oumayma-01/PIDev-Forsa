package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;


public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 50)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  private int idrole;
  @NotBlank
  @Size(min = 6, max = 40)
  private String password;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getIdrole() {
    return idrole;
  }

  public void setIdrole(int idrole) {
    this.idrole = idrole;
  }
}
