package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.GiftService;
import org.example.forsapidev.entities.CreditManagement.Gift;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftService giftService;
    private final UserRepository userRepository;

    @Autowired
    public GiftController(GiftService giftService, UserRepository userRepository) {
        this.giftService = giftService;
        this.userRepository = userRepository;
    }

    /**
     * Frontend helper endpoint:
     * Returns a one-time "gift won" notification flag for the authenticated user.
     * The flag is consumed (reset) when read.
     */
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/me/award-notification")
    public ResponseEntity<?> consumeMyAwardNotification(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Gift consumedGift = giftService.consumeAwardNotification(userId);
        if (consumedGift != null) {
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("show", true);
            if (consumedGift.getAwardedAmount() != null) {
                res.put("amount", consumedGift.getAwardedAmount());
            } else if (consumedGift.getAccumulatedAmount() != null) {
                res.put("amount", consumedGift.getAccumulatedAmount());
            } else {
                res.put("amount", 500.0);
            }
            return ResponseEntity.ok(res);
        } else {
            return ResponseEntity.ok(Map.of("show", false));
        }
    }

    /**
     * Debug/admin endpoint: view a client's current Gift bucket.
     * Note: this exposes accumulated amounts, so it is restricted to AGENT/ADMIN.
     */
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getGiftForClient(@PathVariable Long clientId) {
        Gift gift = giftService.getGiftByClientId(clientId);
        if (gift == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Gift not found"));
        }
        return ResponseEntity.ok(gift);
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl u) {
            return u.getId();
        }

        String name = authentication.getName();
        if (name != null && name.startsWith("id:")) {
            try {
                return Long.parseLong(name.substring(3));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return userRepository.findByUsername(name).map(u -> u.getId()).orElse(null);
    }
}
