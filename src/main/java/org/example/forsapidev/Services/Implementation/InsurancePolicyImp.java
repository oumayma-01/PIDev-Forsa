package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Services.Interfaces.IInsurancePolicy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class InsurancePolicyImp implements IInsurancePolicy {
    InsurancePolicyRepository insurancePolicyRepository;
    InsuranceClaimRepository insuranceClaimRepository;
    PremiumPaymentRepository premiumPaymentRepository;

    public List<InsurancePolicy> retrieveAllInsurancePolicies() {
        return insurancePolicyRepository.findAll();
    }

    public InsurancePolicy retrieveInsurancePolicy(Long policyId) {
        return insurancePolicyRepository.findById(policyId).get();
    }

    public InsurancePolicy addInsurancePolicy(InsurancePolicy policy) {
        return insurancePolicyRepository.save(policy);
    }

    public void removeInsurancePolicy(Long policyId) {
        insurancePolicyRepository.deleteById(policyId);
    }

    public InsurancePolicy modifyInsurancePolicy(InsurancePolicy policy) {
        return insurancePolicyRepository.save(policy);
    }

    @Override
    public InsurancePolicy affectClaimsToPolicy(List<Long> claimIds, Long policyId) {
        InsurancePolicy policy = insurancePolicyRepository.findById(policyId).get();
        List<InsuranceClaim> claims = insuranceClaimRepository.findAllById(claimIds);

        for (InsuranceClaim claim : claims) {
            claim.setInsurancePolicy(policy);
        }

        policy.setClaims(new HashSet<>(claims));
        return insurancePolicyRepository.save(policy);
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