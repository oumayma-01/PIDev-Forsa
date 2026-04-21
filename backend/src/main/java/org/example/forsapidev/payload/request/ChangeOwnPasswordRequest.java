package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeOwnPasswordRequest {
  /** Required for local accounts; ignored for Google-only accounts until they set a password. */
  private String currentPassword;

  @NotBlank
  @Size(min = 6, max = 40)
  private String newPassword;

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
