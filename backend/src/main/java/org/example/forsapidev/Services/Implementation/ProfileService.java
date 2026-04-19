package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.AgentRegistryService;
import org.example.forsapidev.Services.Interfaces.IProfileService;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.ChangeOwnPasswordRequest;
import org.example.forsapidev.payload.request.ProfileUpdateRequest;
import org.example.forsapidev.payload.response.CurrentUserResponse;
import org.example.forsapidev.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
class ProfileService implements IProfileService {

  private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
      MediaType.IMAGE_JPEG_VALUE,
      MediaType.IMAGE_PNG_VALUE,
      "image/webp",
      MediaType.IMAGE_GIF_VALUE
  );

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AgentRegistryService agentRegistryService;

  @Value("${app.profile.upload-dir:uploads/profile-images}")
  private String uploadDir;

  @Value("${app.profile.max-bytes:2097152}")
  private long maxAvatarBytes;

  ProfileService(UserRepository userRepository,
                   PasswordEncoder passwordEncoder,
                   AgentRegistryService agentRegistryService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.agentRegistryService = agentRegistryService;
  }

  @Override
  public CurrentUserResponse getProfile(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return toResponse(user);
  }

  @Override
  @Transactional
  public ResponseEntity<?> updateProfile(Long userId, ProfileUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow();

    if (!Objects.equals(user.getUsername(), request.getUsername())
        && Boolean.TRUE.equals(userRepository.existsByUsername(request.getUsername()))) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken."));
    }
    if (!Objects.equals(user.getEmail(), request.getEmail())
        && Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use."));
    }

    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    userRepository.save(user);
    agentRegistryService.syncAgentForUser(userId);

    return ResponseEntity.ok(toResponse(user));
  }

  @Override
  @Transactional
  public ResponseEntity<?> changePassword(Long userId, ChangeOwnPasswordRequest request) {
    User user = userRepository.findById(userId).orElseThrow();
    boolean googleOnly = "GOOGLE".equalsIgnoreCase(user.getAuthProvider());
    if (googleOnly) {
      user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
      user.setAuthProvider("LOCAL");
      userRepository.save(user);
      return ResponseEntity.ok(new MessageResponse("Password set successfully. You can also sign in with email and password."));
    }
    String current = request.getCurrentPassword();
    if (current == null || current.isBlank()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Current password is required."));
    }
    if (!passwordEncoder.matches(current, user.getPasswordHash())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect."));
    }
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    return ResponseEntity.ok(new MessageResponse("Password updated successfully."));
  }

  @Override
  @Transactional
  public ResponseEntity<?> uploadAvatar(Long userId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Please choose an image file."));
    }
    if (file.getSize() > maxAvatarBytes) {
      return ResponseEntity.badRequest().body(new MessageResponse("Image is too large (max 2 MB)."));
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
      return ResponseEntity.badRequest().body(new MessageResponse("Only JPEG, PNG, WebP, or GIF images are allowed."));
    }

    User user = userRepository.findById(userId).orElseThrow();
    String ext = extensionForContentType(contentType);
    Path dir = resolveUploadDir();
    try {
      Files.createDirectories(dir);
      deleteStoredFile(user.getProfileImageKey(), dir);
      String key = "user-" + userId + "-" + System.nanoTime() + ext;
      Path target = dir.resolve(key).normalize();
      if (!target.startsWith(dir)) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Invalid storage path."));
      }
      file.transferTo(target.toFile());
      user.setProfileImageKey(key);
      userRepository.save(user);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("Could not save the image. Please try again."));
    }
    return ResponseEntity.ok(toResponse(user));
  }

  @Override
  @Transactional
  public ResponseEntity<?> deleteAvatar(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    Path dir = resolveUploadDir();
    deleteStoredFile(user.getProfileImageKey(), dir);
    user.setProfileImageKey(null);
    userRepository.save(user);
    return ResponseEntity.ok(toResponse(user));
  }

  @Override
  public ResponseEntity<byte[]> getAvatar(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    String key = user.getProfileImageKey();
    if (key == null || key.isBlank()) {
      return ResponseEntity.notFound().build();
    }
    Path dir = resolveUploadDir();
    Path file = dir.resolve(key).normalize();
    if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
      return ResponseEntity.notFound().build();
    }
    try {
      byte[] bytes = Files.readAllBytes(file);
      MediaType mediaType = mediaTypeForFilename(key);
      return ResponseEntity.ok()
          .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
          .contentType(mediaType)
          .body(bytes);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private CurrentUserResponse toResponse(User user) {
    List<String> roles = List.of("ROLE_" + user.getRole().getName().name());
    boolean hasImage = user.getProfileImageKey() != null && !user.getProfileImageKey().isBlank();
    boolean oauthAccount = "GOOGLE".equalsIgnoreCase(user.getAuthProvider());
    return new CurrentUserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        roles,
        hasImage,
        oauthAccount
    );
  }

  private Path resolveUploadDir() {
    Path p = Path.of(uploadDir);
    if (!p.isAbsolute()) {
      p = Path.of(System.getProperty("user.dir")).resolve(p).normalize();
    }
    return p;
  }

  private static void deleteStoredFile(String key, Path dir) {
    if (key == null || key.isBlank()) {
      return;
    }
    try {
      Path f = dir.resolve(key).normalize();
      if (f.startsWith(dir)) {
        Files.deleteIfExists(f);
      }
    } catch (IOException ignored) {
    }
  }

  private static String extensionForContentType(String contentType) {
    return switch (contentType) {
      case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
      case MediaType.IMAGE_PNG_VALUE -> ".png";
      case "image/webp" -> ".webp";
      case MediaType.IMAGE_GIF_VALUE -> ".gif";
      default -> ".bin";
    };
  }

  private static MediaType mediaTypeForFilename(String key) {
    String lower = key.toLowerCase();
    if (lower.endsWith(".png")) {
      return MediaType.IMAGE_PNG;
    }
    if (lower.endsWith(".gif")) {
      return MediaType.IMAGE_GIF;
    }
    if (lower.endsWith(".webp")) {
      return MediaType.parseMediaType("image/webp");
    }
    return MediaType.IMAGE_JPEG;
  }
}
