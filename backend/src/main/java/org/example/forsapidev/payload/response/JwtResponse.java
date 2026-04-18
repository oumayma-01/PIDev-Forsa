package org.example.forsapidev.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private Long id;
  private String username;
  private String email;
  private List<String> roles;
  private Boolean hasProfileImage;
  /** True when the user must set a password in profile (Google-created account, no known password yet). */
  private Boolean oauthAccount;

  public JwtResponse(String accessToken, Long id, String username, String email,List<String> roles) {
    this.token = accessToken;
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles;
  }



}
