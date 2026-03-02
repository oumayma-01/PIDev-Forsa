package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.InsurancePolicyApplicationDTO;
import org.example.forsapidev.Services.Interfaces.IIntegratedInsurancePolicyService;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@AllArgsConstructor
@RequestMapping("/integrated-policy")
@CrossOrigin(origins = "*")
public class IntegratedInsurancePolicyController {

    private final IIntegratedInsurancePolicyService integratedPolicyService;

    /**
     * Apply for insurance with automatic actuarial calculations
     * This will:
     * 1. Calculate risk score
     * 2. Calculate premium using actuarial formulas
     * 3. Create policy with all calculated values
     * 4. Generate premium payment schedule automatically
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<?> applyForInsurance(@RequestBody InsurancePolicyApplicationDTO application) {
        try {
            InsurancePolicy policy = integratedPolicyService
                    .createPolicyWithActuarialCalculations(application);

            return ResponseEntity.status(HttpStatus.CREATED).body(policy);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error creating policy: " + e.getMessage());
        }
    }
}