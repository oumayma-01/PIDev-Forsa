package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
  private Long id;
  private String username;
  private String email;
  private List<String> roles;
  /** True when the user has uploaded a profile picture (served at {@code GET /api/profile/me/avatar}). */
  private boolean hasProfileImage;
  /**
   * True when the account was created with Google and the user has not yet set a password in profile
   * ({@code authProvider} is {@code GOOGLE}); current password is not required to set one.
   */
  private boolean oauthAccount;
}
