package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.example.forsapidev.Services.Interfaces.IProfileService;
import org.example.forsapidev.payload.request.ChangeOwnPasswordRequest;
import org.example.forsapidev.payload.request.ProfileUpdateRequest;
import org.example.forsapidev.payload.response.CurrentUserResponse;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/profile")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {

  private final IProfileService profileService;

  public ProfileController(IProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping("/me")
  public ResponseEntity<CurrentUserResponse> getMe() {
    return ResponseEntity.ok(profileService.getProfile(currentUserId()));
  }

  @PutMapping("/me")
  public ResponseEntity<?> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
    return profileService.updateProfile(currentUserId(), request);
  }

  @PutMapping("/me/password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangeOwnPasswordRequest request) {
    return profileService.changePassword(currentUserId(), request);
  }

  @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file) {
    return profileService.uploadAvatar(currentUserId(), file);
  }

  @DeleteMapping("/me/avatar")
  public ResponseEntity<?> deleteAvatar() {
    return profileService.deleteAvatar(currentUserId());
  }

  @GetMapping("/me/avatar")
  public ResponseEntity<byte[]> getAvatar() {
    return profileService.getAvatar(currentUserId());
  }

  private static Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
      throw new IllegalStateException("Not authenticated");
    }
    return ((UserDetailsImpl) auth.getPrincipal()).getId();
  }
}
