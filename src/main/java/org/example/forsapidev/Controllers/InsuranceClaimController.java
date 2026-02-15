package org.example.forsapidev.Controllers;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.Services.Interfaces.IInsuranceClaim;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/insurance-claim")
public class InsuranceClaimController {

    IInsuranceClaim insuranceClaimService;

    @GetMapping("/retrieve-all-insurance-claims")
    public List<InsuranceClaim> retrieveAllInsuranceClaims() {
        List<InsuranceClaim> claims = insuranceClaimService.retrieveAllInsuranceClaims();
        return claims;
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