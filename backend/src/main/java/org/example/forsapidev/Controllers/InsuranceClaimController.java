package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.ClaimTemplate;
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

    private final org.example.forsapidev.Repositories.InsurancePolicyRepository policyRepository;
    private final IInsuranceClaim insuranceClaimService;
    private final org.example.forsapidev.Services.ClaimTemplateService claimTemplateService;

    @GetMapping("/template/{policy-type}")
    public ClaimTemplate getClaimTemplate(@PathVariable("policy-type") String policyType) {
        return claimTemplateService.getTemplateForPolicyType(policyType);
    }

    @GetMapping("/retrieve-all-insurance-claims")
    public List<InsuranceClaim> retrieveAllInsuranceClaims() {
        List<InsuranceClaim> claims = insuranceClaimService.retrieveAllInsuranceClaims();
        return claims;
    }

    @GetMapping("/my-claims")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CLIENT')")
    public List<InsuranceClaim> retrieveMyClaims() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            return null;
        org.example.forsapidev.security.services.UserDetailsImpl userDetails = (org.example.forsapidev.security.services.UserDetailsImpl) authentication
                .getPrincipal();
        // Since we don't have UserRepository here, we can just use the user ID from
        // UserDetailsImpl
        return insuranceClaimService.retrieveMyClaims(userDetails.getId());
    }

    @PostMapping("/add-insurance-claim")
    public InsuranceClaim addInsuranceClaim(@RequestBody InsuranceClaim claim) {
        System.out.println("Received Insurance Claim: " + claim.getClaimNumber());
        if (claim.getInsurancePolicy() != null) {
            System.out.println("For Policy ID: " + claim.getInsurancePolicy().getId());
        }
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

    @PutMapping(value = "/modify-insurance-claim", consumes = "application/json", produces = "application/json")
    public InsuranceClaim modifyInsuranceClaim(@RequestBody InsuranceClaim claim) {
        InsuranceClaim insuranceClaim = insuranceClaimService.modifyInsuranceClaim(claim);
        return insuranceClaim;
    }

    @PostMapping("/upload-attachment")
    public String uploadAttachment(@RequestParam("file") org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {
        return insuranceClaimService.uploadAttachment(file);
    }

    @GetMapping("/attachments/{filename}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getAttachment(
            @PathVariable String filename) {
        org.springframework.core.io.Resource file = insuranceClaimService.getAttachment(filename);
        String contentType = null;
        try {
            contentType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(file.getURI()));
        } catch (java.io.IOException e) {
            // Fallback
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType
                        .parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}