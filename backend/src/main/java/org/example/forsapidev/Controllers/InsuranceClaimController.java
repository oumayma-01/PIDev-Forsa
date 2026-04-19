package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.Services.Interfaces.IInsuranceClaim;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/insurance-claim")
@CrossOrigin(origins = "*")
public class InsuranceClaimController {

    IInsuranceClaim insuranceClaimService;

    @GetMapping("/retrieve-all-insurance-claims")
    public List<InsuranceClaim> retrieveAllInsuranceClaims() {
        List<InsuranceClaim> claims = insuranceClaimService.retrieveAllInsuranceClaims();
        return claims;
    }

    @GetMapping("/my-claims")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CLIENT')")
    public List<InsuranceClaim> retrieveMyClaims() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        org.example.forsapidev.security.services.UserDetailsImpl userDetails = (org.example.forsapidev.security.services.UserDetailsImpl) authentication.getPrincipal();
        // Since we don't have UserRepository here, we can just use the user ID from UserDetailsImpl
        return insuranceClaimService.retrieveMyClaims(userDetails.getId());
    }

    @PostMapping("/add-insurance-claim")
    public InsuranceClaim addInsuranceClaim(@RequestBody InsuranceClaim claim) {
        InsuranceClaim insuranceClaim = insuranceClaimService.addInsuranceClaim(claim);
        return insuranceClaim;
    }

    @GetMapping("/retrieve-insurance-claim/{claim-id}")
    public InsuranceClaim retrieveInsuranceClaim(@PathVariable("claim-id") Long claimId) {
        InsuranceClaim claim = insuranceClaimService.retrieveInsuranceClaim(claimId);
        return claim;
    }

    @DeleteMapping("/remove-insurance-claim/{claim-id}")
    public void removeInsuranceClaim(@PathVariable("claim-id") Long claimId) {
        insuranceClaimService.removeInsuranceClaim(claimId);
    }

    @PutMapping("/modify-insurance-claim")
    public InsuranceClaim modifyInsuranceClaim(@RequestBody InsuranceClaim claim) {
        InsuranceClaim insuranceClaim = insuranceClaimService.modifyInsuranceClaim(claim);
        return insuranceClaim;
    }
}