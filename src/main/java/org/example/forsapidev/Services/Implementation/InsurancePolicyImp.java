package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.*;
import org.example.forsapidev.Repositories.*;
import org.example.forsapidev.Services.Interfaces.IInsuranceAmortizationService;
import org.example.forsapidev.Services.Interfaces.IPremiumCalculationService;
import org.example.forsapidev.entities.InsuranceManagement.*;
import org.example.forsapidev.Services.Interfaces.IInsurancePolicy;
import lombok.AllArgsConstructor;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class InsurancePolicyImp implements IInsurancePolicy {    InsurancePolicyRepository insurancePolicyRepository;
    InsuranceProductRepository insuranceProductRepository;
    UserRepository userRepository;
    PremiumPaymentRepository premiumPaymentRepository;
    InsuranceClaimRepository insuranceClaimRepository;
    IPremiumCalculationService premiumCalculationService;
    IInsuranceAmortizationService amortizationService;

    @Override
    @Transactional
    public InsurancePolicy clientSubmitApplication(InsurancePolicyApplicationDTO application, Long userId)
            throws Exception {

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Validate product
        InsuranceProduct product = insuranceProductRepository.findById(application.getProductId())
                .orElseThrow(() -> new Exception("Insurance product not found"));

        if (!product.getIsActive()) {
            throw new Exception("Product not available");
        }

        // Validate coverage
        BigDecimal requestedCoverage = application.getDesiredCoverage();
        if (requestedCoverage.compareTo(product.getCoverageLimit()) > 0) {
            throw new Exception("Coverage exceeds maximum: " + product.getCoverageLimit());
        }

        // Calculate premium
        PremiumCalculationRequestDTO calculationRequest = new PremiumCalculationRequestDTO();
        calculationRequest.setInsuranceType(product.getPolicyType());
        calculationRequest.setCoverageAmount(requestedCoverage);
        calculationRequest.setDurationMonths(application.getDurationMonths());
        calculationRequest.setPaymentFrequency(application.getPaymentFrequency());
        calculationRequest.setRiskProfile(application.getRiskProfile());

        PremiumCalculationResultDTO premiumResult =
                premiumCalculationService.calculatePremium(calculationRequest);

        // Generate payment schedule
        InsuranceAmortizationScheduleDTO schedule = amortizationService.generateAmortizationSchedule(
                premiumResult.getFinalPremium(),
                premiumResult.getEffectiveAnnualRate(),
                application.getDurationMonths(),
                application.getPaymentFrequency(),
                new Date()
        );

        // Create policy
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber("POL-" + System.currentTimeMillis());
        policy.setStatus(PolicyStatus.PENDING);
        policy.setUser(user);
        policy.setInsuranceProduct(product);
        policy.setCoverageLimit(requestedCoverage);
        policy.setPaymentFrequency(application.getPaymentFrequency());

        // Set actuarial calculations
        policy.setPremiumAmount(premiumResult.getPeriodicPayment());
        policy.setPurePremium(premiumResult.getPurePremium());
        policy.setInventoryPremium(premiumResult.getInventoryPremium());
        policy.setCommercialPremium(premiumResult.getCommercialPremium());
        policy.setFinalPremium(premiumResult.getFinalPremium());
        policy.setPeriodicPaymentAmount(premiumResult.getPeriodicPayment());
        policy.setNumberOfPayments(premiumResult.getNumberOfPayments());
        policy.setEffectiveAnnualRate(premiumResult.getEffectiveAnnualRate());
        policy.setRiskScore(premiumResult.getRiskScore());
        policy.setRiskCategory(premiumResult.getRiskCategory());
        policy.setRiskCoefficient(application.getRiskProfile().getRiskCoefficient());
        policy.setCalculationNotes(premiumResult.getAdditionalNotes());
        policy.setNextPremiumDueDate(schedule.getSchedule().get(0).getDueDate());

        // Save policy
        policy = insurancePolicyRepository.save(policy);

        // Create payment schedule
        for (InsuranceAmortizationLineDTO line : schedule.getSchedule()) {
            PremiumPayment payment = new PremiumPayment();
            payment.setInsurancePolicy(policy);
            payment.setAmount(line.getPayment());
            payment.setDueDate(line.getDueDate());
            payment.setStatus(PaymentStatus.PENDING);
            premiumPaymentRepository.save(payment);
        }

        System.out.println("✅ Policy created: " + policy.getPolicyNumber());
        return policy;
    }

    @Override
    @Transactional
    public InsurancePolicy agentUpdatePolicy(Long policyId, PolicyStatus status,
                                             BigDecimal approvedCoverage, String notes)
            throws Exception {

        InsurancePolicy policy = insurancePolicyRepository.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found"));

        if (policy.getStatus() != PolicyStatus.PENDING) {
            throw new Exception("Can only modify PENDING policies");
        }

        // Update status
        policy.setStatus(status);

        if (status == PolicyStatus.ACTIVE) {
            Date startDate = new Date();
            policy.setStartDate(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, policy.getInsuranceProduct().getDurationMonths());
            policy.setEndDate(calendar.getTime());
        }

        // Update coverage if provided
        if (approvedCoverage != null) {
            policy.setCoverageLimit(approvedCoverage);
        }

        // Add notes if provided
        if (notes != null && !notes.isEmpty()) {
            String existingNotes = policy.getCalculationNotes() != null ?
                    policy.getCalculationNotes() : "";
            policy.setCalculationNotes(existingNotes + "\n\n[Agent] " + notes);
        }

        policy = insurancePolicyRepository.save(policy);
        System.out.println("✅ Policy updated: " + policy.getPolicyNumber());

        return policy;
    }

    // ========== EXISTING CRUD METHODS ==========

    @Override
    public List<InsurancePolicy> retrieveAllInsurancePolicies() {
        return insurancePolicyRepository.findAll();
    }

    @Override
    public InsurancePolicy retrieveInsurancePolicy(Long id) {
        return insurancePolicyRepository.findById(id).orElse(null);
    }

    @Override
    public InsurancePolicy addInsurancePolicy(InsurancePolicy policy) {
        if (policy.getUser() != null && policy.getUser().getId() != null) {
            User user = userRepository.findById(policy.getUser().getId()).orElse(null);
            policy.setUser(user);
        }

        if (policy.getInsuranceProduct() != null && policy.getInsuranceProduct().getId() != null) {
            InsuranceProduct product = insuranceProductRepository.findById(
                    policy.getInsuranceProduct().getId()
            ).orElse(null);
            policy.setInsuranceProduct(product);
        }

        return insurancePolicyRepository.save(policy);
    }

    @Override
    public void removeInsurancePolicy(Long id) {
        insurancePolicyRepository.deleteById(id);
    }

    @Override
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
}