package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IPartnerService;
import org.example.forsapidev.entities.PartnershipManagement.Partner;
import org.example.forsapidev.entities.PartnershipManagement.PartnerStatus;
import org.example.forsapidev.entities.PartnershipManagement.PartnerType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final IPartnerService partnerService;

    @PostMapping
    public ResponseEntity<Partner> createPartner(@RequestBody Partner partner) {
        Partner created = partnerService.createPartner(partner);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner partner) {
        Partner updated = partnerService.updatePartner(id, partner);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partner> getPartner(@PathVariable Long id) {
        Partner partner = partnerService.getPartnerById(id);
        return ResponseEntity.ok(partner);
    }

    @GetMapping
    public ResponseEntity<List<Partner>> getAllPartners() {
        List<Partner> partners = partnerService.getAllPartners();
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Partner>> getPartnersByStatus(@PathVariable PartnerStatus status) {
        List<Partner> partners = partnerService.getPartnersByStatus(status);
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Partner>> getPartnersByType(@PathVariable PartnerType type) {
        List<Partner> partners = partnerService.getPartnersByType(type);
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Partner>> getPartnersByCity(@PathVariable String city) {
        List<Partner> partners = partnerService.getPartnersByCity(city);
        return ResponseEntity.ok(partners);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Partner> activatePartner(@PathVariable Long id) {
        Partner activated = partnerService.activatePartner(id);
        return ResponseEntity.ok(activated);
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Partner> suspendPartner(@PathVariable Long id, @RequestParam String reason) {
        Partner suspended = partnerService.suspendPartner(id, reason);
        return ResponseEntity.ok(suspended);
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Partner> reactivatePartner(@PathVariable Long id) {
        Partner reactivated = partnerService.reactivatePartner(id);
        return ResponseEntity.ok(reactivated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Partner>> getTopRatedPartners() {
        List<Partner> partners = partnerService.getTopRatedPartners();
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Partner>> getPartnersNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Integer radiusKm) {
        List<Partner> partners = partnerService.getPartnersNearby(latitude, longitude, radiusKm);
        return ResponseEntity.ok(partners);
    }
}