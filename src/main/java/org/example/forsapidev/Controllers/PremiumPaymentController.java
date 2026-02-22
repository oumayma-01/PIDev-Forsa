package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.Services.Interfaces.IPremiumPayment;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/premium-payment")
public class PremiumPaymentController {

    IPremiumPayment premiumPaymentService;

    @GetMapping("/retrieve-all-premium-payments")
    public List<PremiumPayment> retrieveAllPremiumPayments() {
        List<PremiumPayment> payments = premiumPaymentService.retrieveAllPremiumPayments();
        return payments;
    }

    @PostMapping("/add-premium-payment")
    public PremiumPayment addPremiumPayment(@RequestBody PremiumPayment payment) {
        PremiumPayment premiumPayment = premiumPaymentService.addPremiumPayment(payment);
        return premiumPayment;
    }

    @GetMapping("/retrieve-premium-payment/{payment-id}")
    public PremiumPayment retrievePremiumPayment(@PathVariable("payment-id") Long paymentId) {
        PremiumPayment payment = premiumPaymentService.retrievePremiumPayment(paymentId);
        return payment;
    }

    @DeleteMapping("/remove-premium-payment/{payment-id}")
    public void removePremiumPayment(@PathVariable("payment-id") Long paymentId) {
        premiumPaymentService.removePremiumPayment(paymentId);
    }

    @PutMapping("/modify-premium-payment")
    public PremiumPayment modifyPremiumPayment(@RequestBody PremiumPayment payment) {
        PremiumPayment premiumPayment = premiumPaymentService.modifyPremiumPayment(payment);
        return premiumPayment;
    }
}