package org.example.forsapidev.Services.scoring;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.ScoringManagement.ScoreFeatures;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreFeatureService {

    @PersistenceContext
    private EntityManager em;

    private final CreditRequestRepository creditRequestRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ScoreFeatures extractFeatures(Long clientId) {
        log.info("Extracting score features for client {}", clientId);

        ScoreFeatures f = new ScoreFeatures();
        f.setClientId(clientId);

        // ── F8: Account age (from user.createdAt) ────────────────────────────
        User user = userRepository.findById(clientId).orElse(null);
        if (user != null && user.getCreatedAt() != null) {
            long days = ChronoUnit.DAYS.between(
                    user.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDateTime.now().toLocalDate());
            f.setAccountAgeMonths(days / 30.0);
        } else {
            f.setAccountAgeMonths(0.0);
        }

        // ── F1-F5 via wallet transactions ─────────────────────────────────────
        Long walletId = findWalletId(clientId);
        if (walletId != null) {
            populateWalletFeatures(f, walletId);
        } else {
            f.setAvgMonthlyIncome(0.0);
            f.setIncomeStability(1.0);
            f.setSavingsRate(0.0);
            f.setCurrentBalance(0.0);
            f.setAccountActivity(0.0);
        }

        // ── F6: Repayment history from credit requests ────────────────────────
        List<CreditRequest> credits = creditRequestRepository.findByUserIdOrderByRequestDateDesc(clientId);
        long repaid = credits.stream().filter(c -> c.getStatus() == CreditStatus.REPAID).count();
        long defaulted = credits.stream().filter(c -> c.getStatus() == CreditStatus.DEFAULTED).count();
        if (repaid + defaulted == 0) {
            f.setRepaymentHistory(0.5); // neutral – no history
        } else {
            f.setRepaymentHistory((double) repaid / (repaid + defaulted));
        }

        // ── F7: Has active credit ─────────────────────────────────────────────
        boolean hasActive = credits.stream().anyMatch(c -> c.getStatus() == CreditStatus.ACTIVE);
        f.setHasActiveCredit(hasActive);

        log.info("Features for client {}: income={}, stability={}, savings={}, balance={}, repayment={}, activeCredit={}, age={}",
                clientId, f.getAvgMonthlyIncome(), f.getIncomeStability(), f.getSavingsRate(),
                f.getCurrentBalance(), f.getRepaymentHistory(), f.getHasActiveCredit(), f.getAccountAgeMonths());

        return f;
    }

    private Long findWalletId(Long clientId) {
        try {
            List<Long> ids = em.createQuery(
                            "SELECT a.wallet.id FROM Account a WHERE a.owner.id = :ownerId AND a.wallet IS NOT NULL",
                            Long.class)
                    .setParameter("ownerId", clientId)
                    .setMaxResults(1)
                    .getResultList();
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception e) {
            log.warn("Could not find wallet for client {}: {}", clientId, e.getMessage());
            return null;
        }
    }

    private void populateWalletFeatures(ScoreFeatures f, Long walletId) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // Current balance
        try {
            List<BigDecimal> balances = em.createQuery(
                    "SELECT w.balance FROM Wallet w WHERE w.id = :wid", BigDecimal.class)
                    .setParameter("wid", walletId)
                    .getResultList();
            f.setCurrentBalance(balances.isEmpty() || balances.get(0) == null ? 0.0
                    : balances.get(0).doubleValue());
        } catch (Exception e) {
            f.setCurrentBalance(0.0);
        }

        // Last 6 months transactions
        List<Transaction> recent;
        try {
            recent = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.wallet.id = :wid AND t.date >= :since",
                    Transaction.class)
                    .setParameter("wid", walletId)
                    .setParameter("since", sixMonthsAgo)
                    .getResultList();
        } catch (Exception e) {
            log.warn("Could not fetch transactions for wallet {}: {}", walletId, e.getMessage());
            recent = Collections.emptyList();
            f.setAvgMonthlyIncome(0.0);
            f.setIncomeStability(1.0);
            f.setSavingsRate(0.0);
            f.setAccountActivity(0.0);
            return;
        }

        // Monthly deposit buckets
        Map<String, Double> monthlyDeposits = new TreeMap<>();
        double totalDeposits = 0;
        double totalWithdrawals = 0;
        int txCount = recent.size();

        for (Transaction t : recent) {
            if (t.getAmount() == null || t.getDate() == null) continue;
            double amt = t.getAmount().doubleValue();
            String month = t.getDate().getYear() + "-" + t.getDate().getMonthValue();

            if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.TRANSFER_IN) {
                totalDeposits += amt;
                monthlyDeposits.merge(month, amt, Double::sum);
            } else if (t.getType() == TransactionType.WITHDRAW || t.getType() == TransactionType.TRANSFER_OUT) {
                totalWithdrawals += amt;
            }
        }

        // F1: avg monthly income
        double avgIncome = totalDeposits / 6.0;
        f.setAvgMonthlyIncome(avgIncome);

        // F2: income stability (coefficient of variation)
        if (monthlyDeposits.size() >= 2) {
            double mean = monthlyDeposits.values().stream().mapToDouble(d -> d).average().orElse(0);
            if (mean > 0) {
                double variance = monthlyDeposits.values().stream()
                        .mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0);
                f.setIncomeStability(Math.sqrt(variance) / mean);
            } else {
                f.setIncomeStability(1.0);
            }
        } else {
            f.setIncomeStability(monthlyDeposits.isEmpty() ? 1.0 : 0.0);
        }

        // F3: savings rate
        f.setSavingsRate(totalDeposits > 0 ? (totalDeposits - totalWithdrawals) / totalDeposits : 0.0);

        // F5: account activity (tx per day since account creation)
        double ageMonths = f.getAccountAgeMonths() != null ? f.getAccountAgeMonths() : 1.0;
        double ageDays = Math.max(ageMonths * 30, 1);
        f.setAccountActivity(txCount / ageDays);
    }
}
