package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.payload.request.ChangeOwnPasswordRequest;
import org.example.forsapidev.payload.request.ProfileUpdateRequest;
import org.example.forsapidev.payload.response.CurrentUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {

  CurrentUserResponse getProfile(Long userId);

  ResponseEntity<?> updateProfile(Long userId, ProfileUpdateRequest request);

  ResponseEntity<?> changePassword(Long userId, ChangeOwnPasswordRequest request);

  ResponseEntity<?> uploadAvatar(Long userId, MultipartFile file);

  ResponseEntity<?> deleteAvatar(Long userId);

  ResponseEntity<byte[]> getAvatar(Long userId);
}
