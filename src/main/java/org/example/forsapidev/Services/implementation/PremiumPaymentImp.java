package org.example.forsapidev.Services.implementation;

import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Services.Interfaces.IPremiumPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class PremiumPaymentImp implements IPremiumPayment {
    PremiumPaymentRepository premiumPaymentRepository;

    public List<PremiumPayment> retrieveAllPremiumPayments() {
        return premiumPaymentRepository.findAll();
    }

    public PremiumPayment retrievePremiumPayment(Long paymentId) {
        return premiumPaymentRepository.findById(paymentId).get();
    }

    public PremiumPayment addPremiumPayment(PremiumPayment payment) {
        return premiumPaymentRepository.save(payment);
    }

    public void removePremiumPayment(Long paymentId) {
        premiumPaymentRepository.deleteById(paymentId);
    }

    public PremiumPayment modifyPremiumPayment(PremiumPayment payment) {
        return premiumPaymentRepository.save(payment);
    }
}