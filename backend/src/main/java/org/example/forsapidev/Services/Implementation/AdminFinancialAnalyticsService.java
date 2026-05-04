package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.DTO.AdminFinancialDashboardDTO;
import org.example.forsapidev.Repositories.AccountRepository;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.TransactionRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.InsuranceManagement.ClaimStatus;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFinancialAnalyticsService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PremiumPaymentRepository premiumPaymentRepository;
    private final CreditRequestRepository creditRequestRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminFinancialDashboardDTO getDashboardAnalytics() {
        List<InsurancePolicy> policies = insurancePolicyRepository.findAll();
        List<InsuranceClaim> claims = insuranceClaimRepository.findAll();
        List<PremiumPayment> premiumPayments = premiumPaymentRepository.findAll();
        List<CreditRequest> credits = creditRequestRepository.findAll();
        List<Account> accounts = accountRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        AdminFinancialDashboardDTO dto = new AdminFinancialDashboardDTO();
        dto.setKpis(buildKpis(premiumPayments, claims, credits, accounts, transactions));
        dto.setMonthlyRevenueVsClaims(buildMonthlySeries(premiumPayments, claims, credits));
        dto.setInsuranceStatusSplit(buildPolicyStatusSplit(policies));
        dto.setCreditStatusSplit(buildCreditStatusSplit(credits));
        dto.setWalletFlowSplit(buildWalletFlowSplit(transactions));
        return dto;
    }

    private AdminFinancialDashboardDTO.FinancialKpiDTO buildKpis(
            List<PremiumPayment> premiumPayments,
            List<InsuranceClaim> claims,
            List<CreditRequest> credits,
            List<Account> accounts,
            List<Transaction> transactions
    ) {
        double totalPremiumCollected = premiumPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PremiumPayment::getAmount)
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double totalClaimsPaid = claims.stream()
                .map(c -> c.getIndemnificationPaid() != null ? c.getIndemnificationPaid() : c.getApprovedAmount())
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        long totalClaims = claims.size();
        long payableClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.PAID).count();
        double claimFrequencyPct = ratioPct(payableClaims, totalClaims);
        double lossRatioPct = ratioPct(totalClaimsPaid, totalPremiumCollected);

        double outstandingPortfolio = credits.stream()
                .map(c -> c.getRemainingBalance() != null ? c.getRemainingBalance() : c.getAmountRequested())
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        long approvedCredits = credits.stream()
                .filter(c -> c.getStatus() == CreditStatus.APPROVED || c.getStatus() == CreditStatus.ACTIVE || c.getStatus() == CreditStatus.REPAID)
                .count();
        double approvalRatePct = ratioPct(approvedCredits, credits.size());

        double weightedAverageInterestRatePct = computeWeightedAverageInterest(credits);
        double portfolioAtRiskPct = computePortfolioAtRisk(credits);

        double totalWalletBalances = accounts.stream()
                .map(Account::getWallet)
                .filter(w -> w != null && w.getBalance() != null)
                .mapToDouble(w -> w.getBalance().doubleValue())
                .sum();

        double inflows = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.INTEREST || t.getType() == TransactionType.TRANSFER_IN)
                .map(Transaction::getAmount)
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        double outflows = transactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW || t.getType() == TransactionType.TRANSFER_OUT)
                .map(Transaction::getAmount)
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        double netCashFlow = inflows - outflows;

        // Solvency margin approximation (insurance domain): (Premium - Claims) / Premium
        double solvencyMarginPct = totalPremiumCollected <= 0 ? 0 : ((totalPremiumCollected - totalClaimsPaid) / totalPremiumCollected) * 100d;

        return new AdminFinancialDashboardDTO.FinancialKpiDTO(
                round2(totalPremiumCollected),
                round2(totalClaimsPaid),
                round2(lossRatioPct),
                round2(claimFrequencyPct),
                round2(outstandingPortfolio),
                round2(approvalRatePct),
                round2(weightedAverageInterestRatePct),
                round2(portfolioAtRiskPct),
                round2(totalWalletBalances),
                round2(netCashFlow),
                round2(solvencyMarginPct)
        );
    }

    private List<AdminFinancialDashboardDTO.MonthlyPointDTO> buildMonthlySeries(
            List<PremiumPayment> premiumPayments,
            List<InsuranceClaim> claims,
            List<CreditRequest> credits
    ) {
        LocalDate now = LocalDate.now();
        Map<YearMonth, AdminFinancialDashboardDTO.MonthlyPointDTO> points = new TreeMap<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now.minusMonths(i));
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            points.put(ym, new AdminFinancialDashboardDTO.MonthlyPointDTO(label, 0, 0, 0));
        }

        for (PremiumPayment payment : premiumPayments) {
            if (payment.getPaidDate() == null || payment.getAmount() == null) continue;
            LocalDateTime paidAt = asLocalDateTime(payment.getPaidDate());
            YearMonth ym = YearMonth.from(paidAt);
            AdminFinancialDashboardDTO.MonthlyPointDTO p = points.get(ym);
            if (p != null) p.setPremiumCollected(p.getPremiumCollected() + payment.getAmount().doubleValue());
        }
        for (InsuranceClaim claim : claims) {
            if (claim.getClaimDate() == null) continue;
            BigDecimal paidAmount = claim.getIndemnificationPaid() != null ? claim.getIndemnificationPaid() : claim.getApprovedAmount();
            if (paidAmount == null) continue;
            LocalDateTime claimAt = asLocalDateTime(claim.getClaimDate());
            YearMonth ym = YearMonth.from(claimAt);
            AdminFinancialDashboardDTO.MonthlyPointDTO p = points.get(ym);
            if (p != null) p.setClaimsPaid(p.getClaimsPaid() + paidAmount.doubleValue());
        }
        for (CreditRequest credit : credits) {
            if (credit.getRequestDate() == null || credit.getAmountRequested() == null) continue;
            YearMonth ym = YearMonth.from(credit.getRequestDate());
            AdminFinancialDashboardDTO.MonthlyPointDTO p = points.get(ym);
            if (p != null) p.setCreditDisbursed(p.getCreditDisbursed() + credit.getAmountRequested().doubleValue());
        }

        return points.values().stream()
                .peek(p -> {
                    p.setPremiumCollected(round2(p.getPremiumCollected()));
                    p.setClaimsPaid(round2(p.getClaimsPaid()));
                    p.setCreditDisbursed(round2(p.getCreditDisbursed()));
                })
                .collect(Collectors.toList());
    }

    private List<AdminFinancialDashboardDTO.LabelValueDTO> buildPolicyStatusSplit(List<InsurancePolicy> policies) {
        Map<PolicyStatus, Long> counts = policies.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(InsurancePolicy::getStatus, () -> new EnumMap<>(PolicyStatus.class), Collectors.counting()));
        List<AdminFinancialDashboardDTO.LabelValueDTO> rows = new ArrayList<>();
        for (Map.Entry<PolicyStatus, Long> entry : counts.entrySet()) {
            rows.add(new AdminFinancialDashboardDTO.LabelValueDTO(entry.getKey().name(), entry.getValue()));
        }
        rows.sort(Comparator.comparing(AdminFinancialDashboardDTO.LabelValueDTO::getLabel));
        return rows;
    }

    private List<AdminFinancialDashboardDTO.LabelValueDTO> buildCreditStatusSplit(List<CreditRequest> credits) {
        Map<CreditStatus, Long> counts = credits.stream()
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.groupingBy(CreditRequest::getStatus, () -> new EnumMap<>(CreditStatus.class), Collectors.counting()));
        List<AdminFinancialDashboardDTO.LabelValueDTO> rows = new ArrayList<>();
        for (Map.Entry<CreditStatus, Long> entry : counts.entrySet()) {
            rows.add(new AdminFinancialDashboardDTO.LabelValueDTO(entry.getKey().name(), entry.getValue()));
        }
        rows.sort(Comparator.comparing(AdminFinancialDashboardDTO.LabelValueDTO::getLabel));
        return rows;
    }

    private List<AdminFinancialDashboardDTO.LabelValueDTO> buildWalletFlowSplit(List<Transaction> transactions) {
        Map<TransactionType, Double> amounts = transactions.stream()
                .filter(t -> t.getType() != null && t.getAmount() != null)
                .collect(Collectors.groupingBy(Transaction::getType, () -> new EnumMap<>(TransactionType.class), Collectors.summingDouble(t -> t.getAmount().doubleValue())));
        List<AdminFinancialDashboardDTO.LabelValueDTO> rows = new ArrayList<>();
        for (Map.Entry<TransactionType, Double> entry : amounts.entrySet()) {
            rows.add(new AdminFinancialDashboardDTO.LabelValueDTO(entry.getKey().name(), round2(entry.getValue())));
        }
        rows.sort(Comparator.comparing(AdminFinancialDashboardDTO.LabelValueDTO::getLabel));
        return rows;
    }

    private double computeWeightedAverageInterest(List<CreditRequest> credits) {
        double weightedSum = 0d;
        double denominator = 0d;
        for (CreditRequest credit : credits) {
            if (credit.getAmountRequested() == null || credit.getInterestRate() == null) continue;
            double amount = credit.getAmountRequested().doubleValue();
            weightedSum += amount * credit.getInterestRate();
            denominator += amount;
        }
        return denominator <= 0 ? 0 : weightedSum / denominator;
    }

    private double computePortfolioAtRisk(List<CreditRequest> credits) {
        double totalOutstanding = 0d;
        double riskyOutstanding = 0d;
        for (CreditRequest credit : credits) {
            BigDecimal exposure = credit.getRemainingBalance() != null ? credit.getRemainingBalance() : credit.getAmountRequested();
            if (exposure == null) continue;
            double value = exposure.doubleValue();
            totalOutstanding += value;
            if (credit.getStatus() == CreditStatus.DEFAULTED || Boolean.TRUE.equals(credit.getIsRisky())) {
                riskyOutstanding += value;
            }
        }
        return ratioPct(riskyOutstanding, totalOutstanding);
    }

    private LocalDateTime asLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    private double ratioPct(double numerator, double denominator) {
        return denominator <= 0 ? 0 : (numerator / denominator) * 100d;
    }

    private double ratioPct(long numerator, long denominator) {
        return denominator <= 0 ? 0 : ((double) numerator / (double) denominator) * 100d;
    }

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }
}
