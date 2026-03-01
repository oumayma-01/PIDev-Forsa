package org.example.forsapidev.Controllers;

import org.example.forsapidev.DTO.*;
import org.example.forsapidev.Services.Interfaces.IInsuranceAmortizationService;
import org.example.forsapidev.Services.Interfaces.IPremiumCalculationService;
import org.example.forsapidev.Services.Interfaces.IInsuranceRiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping("/actuarial")
@CrossOrigin(origins = "*")
public class ActuarialController {

    private final IInsuranceRiskAssessmentService riskAssessmentService;
    private final IPremiumCalculationService premiumCalculationService;
    private final IInsuranceAmortizationService amortizationService;

    /**
     * Calculate risk score for a client
     * Available to: AGENT, ADMIN
     */
    @PostMapping("/risk-assessment")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<InsuranceRiskAssessmentDTO> assessRisk(@RequestBody InsuranceRiskAssessmentDTO riskProfile) {
        try {
            InsuranceRiskAssessmentDTO result = riskAssessmentService.calculateRiskScore(riskProfile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Calculate premium for insurance product
     * Available to: CLIENT, AGENT, ADMIN
     */
    @PostMapping("/calculate-premium")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public ResponseEntity<PremiumCalculationResultDTO> calculatePremium(
            @RequestBody PremiumCalculationRequestDTO request) {
        try {
            PremiumCalculationResultDTO result = premiumCalculationService.calculatePremium(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Generate amortization schedule (tableau d'amortissement)
     * Available to: CLIENT, AGENT, ADMIN
     */
    @GetMapping("/amortization-schedule")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public ResponseEntity<InsuranceAmortizationScheduleDTO> generateAmortizationSchedule(
            @RequestParam BigDecimal principal,
            @RequestParam Double annualRate,
            @RequestParam Integer durationMonths,
            @RequestParam String paymentFrequency) {
        try {
            Date startDate = new Date(); // Start from today
            InsuranceAmortizationScheduleDTO result = amortizationService.generateAmortizationSchedule(
                    principal, annualRate, durationMonths, paymentFrequency, startDate
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Complete insurance quote: risk + premium + amortization
     * Available to: CLIENT, AGENT, ADMIN
     */
    @PostMapping("/complete-quote")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    public ResponseEntity<InsuranceCompleteQuoteDTO> getCompleteQuote(
            @RequestBody PremiumCalculationRequestDTO request) {
        try {
            // 1. Calculate premium
            PremiumCalculationResultDTO premiumResult = premiumCalculationService.calculatePremium(request);

            // 2. Generate amortization schedule
            InsuranceAmortizationScheduleDTO amortization = amortizationService.generateAmortizationSchedule(
                    premiumResult.getFinalPremium(),
                    premiumResult.getEffectiveAnnualRate(),
                    request.getDurationMonths(),
                    request.getPaymentFrequency(),
                    new Date()
            );

            // 3. Build complete quote
            InsuranceCompleteQuoteDTO quote = new InsuranceCompleteQuoteDTO();
            quote.setPremiumDetails(premiumResult);
            quote.setAmortizationSchedule(amortization);

            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}