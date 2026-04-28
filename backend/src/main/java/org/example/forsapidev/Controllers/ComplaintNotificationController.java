package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintNotificationService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.ComplaintNotification;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaint-notifications")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class ComplaintNotificationController {

    private final IComplaintNotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/my-notifications")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public ResponseEntity<List<ComplaintNotification>> getMyNotifications(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(notificationService.getNotificationsForUser(user.getId()));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.ok(Map.of("count", 0L));
        long count = notificationService.countUnreadForUser(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            notificationService.markAllAsReadForUser(user.getId());
        }
        return ResponseEntity.ok().build();
    }
}
