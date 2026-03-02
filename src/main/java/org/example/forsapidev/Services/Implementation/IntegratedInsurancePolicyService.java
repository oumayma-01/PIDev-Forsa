package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Config.ActuarialConstants;
import org.example.forsapidev.DTO.*;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.*;
import org.example.forsapidev.entities.InsuranceManagement.*;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class IntegratedInsurancePolicyService implements IIntegratedInsurancePolicyService {

    private final IPremiumCalculationService premiumCalculationService;
    private final IInsuranceAmortizationService amortizationService;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceProductRepository productRepository;
    private final UserRepository userRepository;
    private final PremiumPaymentRepository paymentRepository;

    public IntegratedInsurancePolicyService(
            IPremiumCalculationService premiumCalculationService,
            IInsuranceAmortizationService amortizationService,
            InsurancePolicyRepository policyRepository,
            InsuranceProductRepository productRepository,
            UserRepository userRepository,
            PremiumPaymentRepository paymentRepository) {
        this.premiumCalculationService = premiumCalculationService;
        this.amortizationService = amortizationService;
        this.policyRepository = policyRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public InsurancePolicy createPolicyWithActuarialCalculations(InsurancePolicyApplicationDTO application) throws Exception {

        // 1. Validate and fetch entities
        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new Exception("User not found"));

        InsuranceProduct product = productRepository.findById(application.getProductId())
                .orElseThrow(() -> new Exception("Insurance product not found"));

        // 2. Prepare premium calculation request
        PremiumCalculationRequestDTO calculationRequest = new PremiumCalculationRequestDTO();
        calculationRequest.setInsuranceType(product.getPolicyType());
        calculationRequest.setCoverageAmount(application.getDesiredCoverage());
        calculationRequest.setDurationMonths(application.getDurationMonths());
        calculationRequest.setPaymentFrequency(application.getPaymentFrequency());
        calculationRequest.setRiskProfile(application.getRiskProfile());

        // 3. Calculate premium using actuarial formulas
        PremiumCalculationResultDTO premiumResult = premiumCalculationService.calculatePremium(calculationRequest);

        // 4. Generate amortization schedule
        InsuranceAmortizationScheduleDTO amortizationSchedule = amortizationService.generateAmortizationSchedule(
                premiumResult.getFinalPremium(),
                premiumResult.getEffectiveAnnualRate(),
                application.getDurationMonths(),
                application.getPaymentFrequency(),
                new Date()
        );

        // 5. Create Insurance Policy with actuarial data
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setUser(user);
        policy.setInsuranceProduct(product);

        // Set coverage and premium amounts
        policy.setCoverageLimit(application.getDesiredCoverage());
        policy.setPremiumAmount(premiumResult.getPeriodicPayment());  // Periodic payment

        // Set actuarial calculation results
        policy.setPurePremium(premiumResult.getPurePremium());
        policy.setInventoryPremium(premiumResult.getInventoryPremium());
        policy.setCommercialPremium(premiumResult.getCommercialPremium());
        policy.setFinalPremium(premiumResult.getFinalPremium());

        // Set risk assessment results
        policy.setRiskScore(premiumResult.getRiskScore());
        policy.setRiskCategory(premiumResult.getRiskCategory());
        policy.setRiskCoefficient(application.getRiskProfile().getRiskCoefficient());

        // Set payment details
        policy.setPaymentFrequency(application.getPaymentFrequency());
        policy.setPeriodicPaymentAmount(premiumResult.getPeriodicPayment());
        policy.setNumberOfPayments(premiumResult.getNumberOfPayments());
        policy.setEffectiveAnnualRate(premiumResult.getEffectiveAnnualRate());

        // Set dates
        policy.setStartDate(new Date());
        policy.setEndDate(calculateEndDate(application.getDurationMonths()));
        policy.setNextPremiumDueDate(amortizationSchedule.getSchedule().get(0).getDueDate());

        // Set status and notes
        policy.setStatus(PolicyStatus.PENDING);
        policy.setCalculationNotes(premiumResult.getAdditionalNotes());

        // 6. Save policy first
        policy = policyRepository.save(policy);

        // 7. Create premium payment schedule
        Set<PremiumPayment> payments = createPremiumPayments(policy, amortizationSchedule);
        policy.setPremiumPayments(payments);

        // 8. Save premium payments
        for (PremiumPayment payment : payments) {
            paymentRepository.save(payment);
        }

        System.out.println("✅ Policy created with actuarial calculations: " + policy.getPolicyNumber());
        System.out.println("   - Final Premium: " + premiumResult.getFinalPremium());
        System.out.println("   - Periodic Payment: " + premiumResult.getPeriodicPayment());
        System.out.println("   - Number of Payments: " + premiumResult.getNumberOfPayments());
        System.out.println("   - Risk Category: " + premiumResult.getRiskCategory());

        return policy;
    }

    private Set<PremiumPayment> createPremiumPayments(InsurancePolicy policy,
                                                      InsuranceAmortizationScheduleDTO schedule) {
        Set<PremiumPayment> payments = new HashSet<>();

        for (InsuranceAmortizationLineDTO line : schedule.getSchedule()) {
            PremiumPayment payment = new PremiumPayment();
            payment.setInsurancePolicy(policy);
            payment.setAmount(line.getPayment());
            payment.setDueDate(line.getDueDate());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaidDate(null);
            payment.setTransactionId(null);

            payments.add(payment);
        }

        return payments;
    }

    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis();
    }

    private Date calculateEndDate(Integer durationMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, durationMonths);
        return calendar.getTime();
    }
}