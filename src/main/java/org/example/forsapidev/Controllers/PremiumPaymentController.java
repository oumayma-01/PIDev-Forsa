package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Services.Interfaces.IPremiumPayment;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/premium-payment")
public class PremiumPaymentController {

    IPremiumPayment premiumPaymentService;
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/retrieve-all-premium-payments")
    public List<PremiumPayment> retrieveAllPremiumPayments() {
        List<PremiumPayment> payments = premiumPaymentService.retrieveAllPremiumPayments();
        return payments;
    }

    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    @PostMapping("/add-premium-payment")
    public PremiumPayment addPremiumPayment(@RequestBody PremiumPayment payment) {
        PremiumPayment premiumPayment = premiumPaymentService.addPremiumPayment(payment);
        return premiumPayment;
    }
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-premium-payment/{payment-id}")
    public PremiumPayment retrievePremiumPayment(@PathVariable("payment-id") Long paymentId) {
        PremiumPayment payment = premiumPaymentService.retrievePremiumPayment(paymentId);
        return payment;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/remove-premium-payment/{payment-id}")
    public void removePremiumPayment(@PathVariable("payment-id") Long paymentId) {
        premiumPaymentService.removePremiumPayment(paymentId);
    }
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PutMapping("/modify-premium-payment")
    public PremiumPayment modifyPremiumPayment(@RequestBody PremiumPayment payment) {
        PremiumPayment premiumPayment = premiumPaymentService.modifyPremiumPayment(payment);
        return premiumPayment;
    }

    @PutMapping("/affect-premium-payments/{policy-id}")
    public InsurancePolicy affectPremiumPaymentsToPolicy(@RequestBody List<Long> paymentIds, @PathVariable("policy-id") Long policyId) {
        return premiumPaymentService.affectPremiumPaymentsToPolicy(paymentIds, policyId);
    }
}