package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IFraudDetectionService;
import org.example.forsapidev.entities.PartnershipManagement.FraudAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud-alerts")
@RequiredArgsConstructor
public class FraudAlertController {

    private final IFraudDetectionService fraudDetectionService;

    @GetMapping("/unresolved")
    public ResponseEntity<List<FraudAlert>> getUnresolvedAlerts() {
        List<FraudAlert> alerts = fraudDetectionService.getUnresolvedAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FraudAlert>> getClientAlerts(@PathVariable Long clientId) {
        List<FraudAlert> alerts = fraudDetectionService.getClientAlerts(clientId);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(
            @PathVariable Long id,
            @RequestParam String resolvedBy,
            @RequestParam String notes) {
        fraudDetectionService.resolveAlert(id, resolvedBy, notes);
        return ResponseEntity.ok().build();
    }
}