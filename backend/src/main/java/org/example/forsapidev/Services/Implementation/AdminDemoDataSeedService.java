package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.AccountRepository;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.TransactionRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.CreditManagement.AmortizationType;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.InsuranceManagement.ClaimStatus;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.AccountStatus;
import org.example.forsapidev.entities.WalletManagement.AccountType;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionStatus;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.example.forsapidev.entities.WalletManagement.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AdminDemoDataSeedService {

    private final UserRepository userRepository;
    private final InsuranceProductRepository insuranceProductRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PremiumPaymentRepository premiumPaymentRepository;
    private final CreditRequestRepository creditRequestRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Map<String, Object> generateDemoData(int monthsBack, int recordsPerMonth) {
        int safeMonths = Math.max(3, Math.min(monthsBack, 36));
        int safeRecords = Math.max(5, Math.min(recordsPerMonth, 120));
        Random random = ThreadLocalRandom.current();

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new IllegalStateException("No users found in DB. Create at least one user before seeding.");
        }

        List<InsuranceProduct> products = ensureProducts(random);

        int policiesCreated = 0;
        int claimsCreated = 0;
        int premiumPaymentsCreated = 0;
        int creditsCreated = 0;
        int accountsCreated = 0;
        int transactionsCreated = 0;

        for (int m = safeMonths - 1; m >= 0; m--) {
            LocalDate monthDate = LocalDate.now().minusMonths(m);
            for (int i = 0; i < safeRecords; i++) {
                User user = users.get(random.nextInt(users.size()));
                InsuranceProduct product = products.get(random.nextInt(products.size()));

                InsurancePolicy policy = buildPolicy(user, product, monthDate, random);
                policy = insurancePolicyRepository.save(policy);
                policiesCreated++;

                PremiumPayment payment = buildPremiumPayment(policy, monthDate, random);
                premiumPaymentRepository.save(payment);
                premiumPaymentsCreated++;

                if (random.nextDouble() < 0.55) {
                    InsuranceClaim claim = buildClaim(policy, monthDate, random);
                    insuranceClaimRepository.save(claim);
                    claimsCreated++;
                }

                CreditRequest credit = buildCredit(user, monthDate, random);
                creditRequestRepository.save(credit);
                creditsCreated++;

                if (random.nextDouble() < 0.65) {
                    Account account = buildAccount(user, monthDate, random);
                    account = accountRepository.save(account);
                    accountsCreated++;

                    List<Transaction> txs = buildTransactions(account.getWallet(), monthDate, random);
                    transactionRepository.saveAll(txs);
                    transactionsCreated += txs.size();
                }
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("message", "Demo financial and insurance data generated successfully.");
        out.put("monthsBack", safeMonths);
        out.put("recordsPerMonth", safeRecords);
        out.put("policiesCreated", policiesCreated);
        out.put("premiumPaymentsCreated", premiumPaymentsCreated);
        out.put("claimsCreated", claimsCreated);
        out.put("creditsCreated", creditsCreated);
        out.put("accountsCreated", accountsCreated);
        out.put("transactionsCreated", transactionsCreated);
        return out;
    }

    private List<InsuranceProduct> ensureProducts(Random random) {
        List<InsuranceProduct> existing = insuranceProductRepository.findAll();
        if (!existing.isEmpty()) {
            return existing;
        }

        List<InsuranceProduct> products = new ArrayList<>();
        products.add(makeProduct("Life Guard Basic", "LIFE", bd(45), bd(18000), 12));
        products.add(makeProduct("Health Plus", "HEALTH", bd(62), bd(25000), 12));
        products.add(makeProduct("Auto Protect", "AUTO", bd(78), bd(30000), 12));
        products.add(makeProduct("Small Business Shield", "BUSINESS", bd(90), bd(55000), 24));
        products.add(makeProduct("Crop Secure", "CROP", bd(36), bd(14000), 12));
        return insuranceProductRepository.saveAll(products);
    }

    private InsuranceProduct makeProduct(String name, String type, BigDecimal premium, BigDecimal coverage, int durationMonths) {
        InsuranceProduct p = new InsuranceProduct();
        p.setProductName(name);
        p.setPolicyType(type);
        p.setDescription("Auto-generated demo product");
        p.setPremiumAmount(premium);
        p.setCoverageLimit(coverage);
        p.setDurationMonths(durationMonths);
        p.setIsActive(true);
        return p;
    }

    private InsurancePolicy buildPolicy(User user, InsuranceProduct product, LocalDate monthDate, Random random) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber("POL-" + System.nanoTime() + "-" + random.nextInt(10_000));
        policy.setUser(user);
        policy.setInsuranceProduct(product);
        policy.setCoverageLimit(product.getCoverageLimit());
        policy.setPremiumAmount(product.getPremiumAmount());
        policy.setPeriodicPaymentAmount(product.getPremiumAmount());
        policy.setPaymentFrequency("MONTHLY");
        policy.setNumberOfPayments(product.getDurationMonths());
        policy.setEffectiveAnnualRate(2.5 + random.nextDouble() * 6.0);
        policy.setRiskScore(0.2 + random.nextDouble() * 0.7);
        policy.setRiskCategory(policy.getRiskScore() < 0.4 ? "LOW_RISK" : (policy.getRiskScore() < 0.7 ? "MEDIUM_RISK" : "HIGH_RISK"));
        policy.setRiskCoefficient(0.8 + random.nextDouble() * 0.7);
        policy.setPurePremium(scale2(product.getPremiumAmount().multiply(bd(0.75 + random.nextDouble() * 0.2))));
        policy.setInventoryPremium(scale2(product.getPremiumAmount().multiply(bd(0.9 + random.nextDouble() * 0.25))));
        policy.setCommercialPremium(scale2(product.getPremiumAmount().multiply(bd(1.0 + random.nextDouble() * 0.3))));
        policy.setFinalPremium(scale2(product.getPremiumAmount().multiply(bd(1.1 + random.nextDouble() * 0.35))));

        LocalDate start = monthDate.withDayOfMonth(Math.min(25, 1 + random.nextInt(28)));
        policy.setStartDate(toDate(start));
        policy.setEndDate(toDate(start.plusMonths(product.getDurationMonths())));
        policy.setNextPremiumDueDate(toDate(start.plusMonths(1)));
        policy.setStatus(pickPolicyStatus(random));
        return policy;
    }

    private PremiumPayment buildPremiumPayment(InsurancePolicy policy, LocalDate monthDate, Random random) {
        PremiumPayment p = new PremiumPayment();
        p.setInsurancePolicy(policy);
        p.setAmount(policy.getPeriodicPaymentAmount() != null ? policy.getPeriodicPaymentAmount() : policy.getPremiumAmount());
        LocalDate due = monthDate.withDayOfMonth(Math.min(26, 1 + random.nextInt(27)));
        p.setDueDate(toDate(due));
        PaymentStatus status = pickPaymentStatus(random);
        p.setStatus(status);
        if (status == PaymentStatus.PAID) {
            p.setPaidDate(toDateTime(due.plusDays(random.nextInt(5))));
            p.setTransactionId(Math.abs(random.nextLong()));
        }
        return p;
    }

    private InsuranceClaim buildClaim(InsurancePolicy policy, LocalDate monthDate, Random random) {
        InsuranceClaim claim = new InsuranceClaim();
        claim.setInsurancePolicy(policy);
        claim.setClaimNumber("CLM-" + System.nanoTime() + "-" + random.nextInt(10_000));
        LocalDate incident = monthDate.withDayOfMonth(Math.min(22, 1 + random.nextInt(24)));
        claim.setIncidentDate(toDate(incident));
        claim.setClaimDate(toDateTime(incident.plusDays(random.nextInt(6))));
        BigDecimal amount = bd(150 + random.nextInt(4_500));
        claim.setClaimAmount(amount);
        ClaimStatus status = pickClaimStatus(random);
        claim.setStatus(status);
        if (status == ClaimStatus.APPROVED || status == ClaimStatus.PAID) {
            BigDecimal approved = scale2(amount.multiply(bd(0.55 + random.nextDouble() * 0.4)));
            claim.setApprovedAmount(approved);
            claim.setIndemnificationPaid(status == ClaimStatus.PAID ? approved : BigDecimal.ZERO);
        }
        claim.setDescription("Auto-generated claim for analytics demo");
        claim.setAccidentType("STANDARD");
        claim.setClaimSubtype("GENERAL");
        return claim;
    }

    private CreditRequest buildCredit(User user, LocalDate monthDate, Random random) {
        CreditRequest credit = new CreditRequest();
        BigDecimal amount = bd(1000 + random.nextInt(50_000));
        credit.setAmountRequested(amount);
        credit.setUser(user);
        credit.setRequestDate(monthDate.atTime(8 + random.nextInt(10), random.nextInt(59)));
        credit.setDurationMonths(6 + random.nextInt(30));
        credit.setInterestRate(5 + random.nextDouble() * 8);
        credit.setTypeCalcul(random.nextBoolean() ? AmortizationType.AMORTISSEMENT_CONSTANT : AmortizationType.ANNUITE_CONSTANTE);
        CreditStatus status = pickCreditStatus(random);
        credit.setStatus(status);
        credit.setIsRisky(random.nextDouble() < 0.18);
        credit.setPaidInstallments(random.nextInt(Math.max(1, credit.getDurationMonths())));

        if (status == CreditStatus.REPAID) {
            credit.setRemainingBalance(BigDecimal.ZERO);
        } else {
            double factor = switch (status) {
                case SUBMITTED, UNDER_REVIEW -> 1.0;
                case APPROVED -> 0.9;
                case ACTIVE -> 0.45 + random.nextDouble() * 0.45;
                case DEFAULTED -> 0.55 + random.nextDouble() * 0.35;
                default -> 1.0;
            };
            credit.setRemainingBalance(scale2(amount.multiply(bd(factor))));
        }
        return credit;
    }

    private Account buildAccount(User user, LocalDate monthDate, Random random) {
        Wallet wallet = new Wallet();
        wallet.setOwnerId(user.getId());
        wallet.setBalance(scale2(bd(350 + random.nextInt(15_000))));

        Account account = new Account();
        account.setOwner(user);
        account.setAccountHolderName(user.getUsername() != null ? user.getUsername() : ("User-" + user.getId()));
        account.setType(random.nextBoolean() ? AccountType.SAVINGS : AccountType.INVESTMENT);
        account.setStatus(random.nextDouble() < 0.92 ? AccountStatus.ACTIVE : AccountStatus.BLOCKED);
        account.setWallet(wallet);
        return account;
    }

    private List<Transaction> buildTransactions(Wallet wallet, LocalDate monthDate, Random random) {
        int count = 2 + random.nextInt(6);
        List<Transaction> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Transaction tx = new Transaction();
            tx.setWallet(wallet);
            tx.setDate(monthDate.atTime(8 + random.nextInt(10), random.nextInt(59)));
            tx.setType(pickTransactionType(random));
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setAmount(scale2(bd(20 + random.nextInt(1600))));
            rows.add(tx);
        }
        return rows;
    }

    private PolicyStatus pickPolicyStatus(Random random) {
        double p = random.nextDouble();
        if (p < 0.62) return PolicyStatus.ACTIVE;
        if (p < 0.76) return PolicyStatus.PENDING;
        if (p < 0.86) return PolicyStatus.SUSPENDED;
        if (p < 0.94) return PolicyStatus.CLAIMED;
        return PolicyStatus.CANCELLED;
    }

    private PaymentStatus pickPaymentStatus(Random random) {
        double p = random.nextDouble();
        if (p < 0.72) return PaymentStatus.PAID;
        if (p < 0.89) return PaymentStatus.PENDING;
        if (p < 0.97) return PaymentStatus.OVERDUE;
        return PaymentStatus.CANCELLED;
    }

    private ClaimStatus pickClaimStatus(Random random) {
        double p = random.nextDouble();
        if (p < 0.35) return ClaimStatus.SUBMITTED;
        if (p < 0.60) return ClaimStatus.UNDER_REVIEW;
        if (p < 0.82) return ClaimStatus.APPROVED;
        if (p < 0.92) return ClaimStatus.PAID;
        return ClaimStatus.REJECTED;
    }

    private CreditStatus pickCreditStatus(Random random) {
        double p = random.nextDouble();
        if (p < 0.18) return CreditStatus.SUBMITTED;
        if (p < 0.34) return CreditStatus.UNDER_REVIEW;
        if (p < 0.54) return CreditStatus.APPROVED;
        if (p < 0.80) return CreditStatus.ACTIVE;
        if (p < 0.92) return CreditStatus.REPAID;
        return CreditStatus.DEFAULTED;
    }

    private TransactionType pickTransactionType(Random random) {
        double p = random.nextDouble();
        if (p < 0.34) return TransactionType.DEPOSIT;
        if (p < 0.50) return TransactionType.WITHDRAW;
        if (p < 0.62) return TransactionType.INTEREST;
        if (p < 0.81) return TransactionType.TRANSFER_IN;
        return TransactionType.TRANSFER_OUT;
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date toDateTime(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
