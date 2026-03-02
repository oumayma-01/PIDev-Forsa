package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.InsurancePolicyApplicationDTO;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Services.Interfaces.IInsurancePolicy;
import lombok.AllArgsConstructor;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/insurance-policy")
public class InsurancePolicyController {

    IInsurancePolicy insurancePolicyService;
    UserRepository userRepository;  // ← ADD THIS

    /**
     * CLIENT: Submit policy application
     * Extracts user ID from JWT token via SecurityContextHolder
     */
    @PostMapping("/client-apply")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> clientApplyForPolicy(@RequestBody InsurancePolicyApplicationDTO application) {
        try {
            // Get authentication from SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            // Extract username from JWT token
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Find user by username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new Exception("User not found: " + username));

            Long userId = user.getId();

            System.out.println("✅ Policy application from user: " + username + " (ID: " + userId + ")");

            InsurancePolicy policy = insurancePolicyService.clientSubmitApplication(application, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(policy);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    /**
     * AGENT: Approve/reject policy
     * Simple parameters - no DTO needed
     */
    @PutMapping("/agent-review/{policy-id}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<?> agentReviewPolicy(
            @PathVariable("policy-id") Long policyId,
            @RequestParam PolicyStatus status,
            @RequestParam(required = false) BigDecimal approvedCoverage,
            @RequestParam(required = false) String notes) {
        try {
            InsurancePolicy policy = insurancePolicyService.agentUpdatePolicy(
                    policyId, status, approvedCoverage, notes
            );
            return ResponseEntity.ok(policy);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ========== EXISTING METHODS (Keep as-is) ==========

    @GetMapping("/retrieve-all-insurance-policies")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public List<InsurancePolicy> retrieveAllInsurancePolicies() {
        return insurancePolicyService.retrieveAllInsurancePolicies();
    }

    @GetMapping("/retrieve-insurance-policy/{policy-id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public InsurancePolicy retrieveInsurancePolicy(@PathVariable("policy-id") Long policyId) {
        return insurancePolicyService.retrieveInsurancePolicy(policyId);
    }

    @DeleteMapping("/remove-insurance-policy/{policy-id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeInsurancePolicy(@PathVariable("policy-id") Long policyId) {
        insurancePolicyService.removeInsurancePolicy(policyId);
    }

    @PostMapping("/add-insurance-policy")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public InsurancePolicy addInsurancePolicy(@RequestBody InsurancePolicy policy) {
        return insurancePolicyService.addInsurancePolicy(policy);
    }

    @PutMapping("/modify-insurance-policy")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public InsurancePolicy modifyInsurancePolicy(@RequestBody InsurancePolicy policy) {
        return insurancePolicyService.modifyInsurancePolicy(policy);
    }
}