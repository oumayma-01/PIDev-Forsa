package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Services.Interfaces.IPremiumPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class PremiumPaymentImp implements IPremiumPayment {
    PremiumPaymentRepository premiumPaymentRepository;
    InsurancePolicyRepository insurancePolicyRepository;

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

    @Override
    public InsurancePolicy affectPremiumPaymentsToPolicy(List<Long> paymentIds, Long policyId) {
        InsurancePolicy policy = insurancePolicyRepository.findById(policyId).get();
        List<PremiumPayment> payments = premiumPaymentRepository.findAllById(paymentIds);

        for (PremiumPayment payment : payments) {
            payment.setInsurancePolicy(policy);
        }

        policy.setPremiumPayments(new HashSet<>(payments));
        return insurancePolicyRepository.save(policy);
    }
}