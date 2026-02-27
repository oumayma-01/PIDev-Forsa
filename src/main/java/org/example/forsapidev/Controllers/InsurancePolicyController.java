package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Services.Interfaces.IInsurancePolicy;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/insurance-policy")
public class InsurancePolicyController {

    IInsurancePolicy insurancePolicyService;

    @GetMapping("/retrieve-all-insurance-policies")
    public List<InsurancePolicy> retrieveAllInsurancePolicies() {
        List<InsurancePolicy> policies = insurancePolicyService.retrieveAllInsurancePolicies();
        return policies;
    }

    @PostMapping("/add-insurance-policy")
    public InsurancePolicy addInsurancePolicy(@RequestBody InsurancePolicy policy) {
        InsurancePolicy insurancePolicy = insurancePolicyService.addInsurancePolicy(policy);
        return insurancePolicy;
    }

    @GetMapping("/retrieve-insurance-policy/{policy-id}")
    public InsurancePolicy retrieveInsurancePolicy(@PathVariable("policy-id") Long policyId) {
        InsurancePolicy policy = insurancePolicyService.retrieveInsurancePolicy(policyId);
        return policy;
    }

    @DeleteMapping("/remove-insurance-policy/{policy-id}")
    public void removeInsurancePolicy(@PathVariable("policy-id") Long policyId) {
        insurancePolicyService.removeInsurancePolicy(policyId);
    }

    @PutMapping("/modify-insurance-policy")
    public InsurancePolicy modifyInsurancePolicy(@RequestBody InsurancePolicy policy) {
        InsurancePolicy insurancePolicy = insurancePolicyService.modifyInsurancePolicy(policy);
        return insurancePolicy;
    }

    @PutMapping("/affect-claims/{policy-id}")
    public InsurancePolicy affectClaimsToPolicy(@RequestBody List<Long> claimIds, @PathVariable("policy-id") Long policyId) {
        return insurancePolicyService.affectClaimsToPolicy(claimIds, policyId);
    }

    @PutMapping("/affect-premium-payments/{policy-id}")
    public InsurancePolicy affectPremiumPaymentsToPolicy(@RequestBody List<Long> paymentIds, @PathVariable("policy-id") Long policyId) {
        return insurancePolicyService.affectPremiumPaymentsToPolicy(paymentIds, policyId);
    }
}