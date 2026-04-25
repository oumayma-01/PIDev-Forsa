package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.AdaptiveInterestResultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Repositories.*;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.entities.WalletManagement.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;
    private final ActivityRepository activityRepo;
    private final UserRepository userRepo;
    private final WalletAiService walletAiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccountServiceImpl(AccountRepository accountRepo,
                              WalletRepository walletRepo,
                              TransactionRepository transactionRepo,
                              ActivityRepository activityRepo,
                              UserRepository userRepo,
                              WalletAiService walletAiService) {
        this.accountRepo = accountRepo;
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
        this.activityRepo = activityRepo;
        this.userRepo = userRepo;
        this.walletAiService = walletAiService;
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private Account findAccount(Long accountId) {
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

    private Wallet findWallet(Long accountId) {
        return findAccount(accountId).getWallet();
    }

    /**
     * Verifies that the requesting user owns this account.
     * Call this before any financial operation.
     */
    private void verifyOwnership(Account account, Long requestingUserId) {
        User owner = account.getOwner();
        if (owner == null || !owner.getId().equals(requestingUserId)) {
            throw new RuntimeException("Access denied: this account does not belong to you");
        }
    }

    private void logActivity(Wallet wallet, String action) {
        Activity activity = new Activity();
        activity.setAction(action);
        activity.setTimestamp(LocalDateTime.now());
        activity.setWallet(wallet);
        activityRepo.save(activity);
    }

    // ── ONLY CHANGE: added tx.setStatus(TransactionStatus.COMPLETED) ──────────
    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType(type);
        tx.setStatus(TransactionStatus.COMPLETED); // every transaction completes instantly
        tx.setWallet(wallet);
        transactionRepo.save(tx);
    }

    // ── ACCOUNT CRUD ──────────────────────────────────────────────────────────

    @Transactional
    @Override
    public Account createAccount(Long ownerId, String type) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found: " + ownerId));

        Wallet wallet = new Wallet();
        wallet.setOwnerId(ownerId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepo.save(wallet);

        Account account = new Account();
        account.setWallet(wallet);
        account.setOwner(owner);
        account.setAccountHolderName(owner.getUsername());

        if (type.equalsIgnoreCase("INVESTMENT")) {
            account.setType(AccountType.INVESTMENT);
            account.setStatus(AccountStatus.BLOCKED);
        } else {
            account.setType(AccountType.SAVINGS);
            account.setStatus(AccountStatus.ACTIVE);
        }

        Account saved = accountRepo.save(account);
        logActivity(wallet, "Account created — type: " + type.toUpperCase()
                + " | Account ID: " + saved.getId());
        return saved;
    }

    @Override
    public Account getAccount(Long accountId) {
        return findAccount(accountId);
    }

    @Override
    public List<Account> getAccountsByOwner(Long ownerId) {
        return accountRepo.findAll().stream()
                .filter(a -> a.getOwner() != null
                        && ownerId.equals(a.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    @Override
    @Transactional
    public Account updateAccountStatus(Long accountId, String status) {
        Account account = findAccount(accountId);
        account.setStatus(AccountStatus.valueOf(status.toUpperCase()));
        Account saved = accountRepo.save(account);
        logActivity(account.getWallet(), "Status changed to: " + status.toUpperCase());
        return saved;
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();
        activityRepo.deleteAll(activityRepo.findByWallet_Id(wallet.getId()));
        transactionRepo.deleteAll(wallet.getTransactions());
        accountRepo.delete(account);
        walletRepo.delete(wallet);
    }

    // ── FINANCIAL OPERATIONS ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void deposit(Long accountId, BigDecimal amount, Long requestingUserId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Deposit amount must be positive");

        Account account = findAccount(accountId);
        verifyOwnership(account, requestingUserId);

        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked — contact support");

        Wallet wallet = account.getWallet();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);
        saveTransaction(wallet, amount, TransactionType.DEPOSIT);
        logActivity(wallet, "Deposit: +" + amount + " TND");
    }

    @Override
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount, Long requestingUserId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Withdrawal amount must be positive");

        Account account = findAccount(accountId);
        verifyOwnership(account, requestingUserId);

        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked");

        Wallet wallet = account.getWallet();
        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);
        saveTransaction(wallet, amount, TransactionType.WITHDRAW);
        logActivity(wallet, "Withdrawal: -" + amount + " TND");
    }

    @Override
    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId,
                         BigDecimal amount, Long requestingUserId) {
        if (fromAccountId.equals(toAccountId))
            throw new RuntimeException("Cannot transfer to the same account");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Transfer amount must be positive");

        Account from = findAccount(fromAccountId);
        verifyOwnership(from, requestingUserId);

        Account to = findAccount(toAccountId);

        if (from.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Your account is blocked");
        if (to.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Destination account is blocked");

        Wallet fromWallet = from.getWallet();
        Wallet toWallet = to.getWallet();

        if (fromWallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepo.save(fromWallet);
        walletRepo.save(toWallet);

        saveTransaction(fromWallet, amount, TransactionType.TRANSFER_OUT);
        saveTransaction(toWallet, amount, TransactionType.TRANSFER_IN);

        logActivity(fromWallet, "Transfer sent: -" + amount + " TND → account #" + toAccountId);
        logActivity(toWallet, "Transfer received: +" + amount + " TND ← account #" + fromAccountId);
    }

    @Override
    @Transactional
    public void applyMonthlyInterest() {
        for (Account account : accountRepo.findAll()) {
            if (account.getType() == AccountType.INVESTMENT
                    && account.getStatus() == AccountStatus.ACTIVE) {
                Wallet wallet = account.getWallet();
                BigDecimal interest = wallet.getBalance()
                        .multiply(new BigDecimal("0.01"))
                        .setScale(2, RoundingMode.HALF_UP);
                wallet.setBalance(wallet.getBalance().add(interest));
                walletRepo.save(wallet);
                saveTransaction(wallet, interest, TransactionType.INTEREST);
                logActivity(wallet, "Fixed monthly interest applied: +" + interest + " TND (1%)");
            }
        }
    }

    @Override
    public BigDecimal getBalance(Long accountId) {
        return findWallet(accountId).getBalance();
    }

    // ── QUERIES ───────────────────────────────────────────────────────────────

    @Override
    public WalletStatisticsDTO getStatistics(Long accountId) {
        Wallet wallet = findWallet(accountId);
        BigDecimal deposits = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal withdrawals = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.WITHDRAW);
        int totalCount = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;
        return new WalletStatisticsDTO(wallet.getBalance(), deposits, withdrawals, totalCount);
    }

    @Override
    public List<Transaction> filterTransactions(Long accountId, TransactionType type) {
        return findWallet(accountId).getTransactions().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Activity> getActivities(Long accountId) {
        return activityRepo.findByWallet_Id(findWallet(accountId).getId());
    }

    // ── AI METHODS ────────────────────────────────────────────────────────────

    @Override
    public WalletForecastDTO forecastBalance(Long accountId, int days) {
        Wallet wallet = findWallet(accountId);
        BigDecimal currentBalance = wallet.getBalance();
        List<Transaction> transactions = wallet.getTransactions();
        int txCount = transactions != null ? transactions.size() : 0;

        BigDecimal totalNet = BigDecimal.ZERO;
        if (transactions != null) {
            for (Transaction tx : transactions) {
                switch (tx.getType()) {
                    case DEPOSIT, TRANSFER_IN, INTEREST ->
                            totalNet = totalNet.add(tx.getAmount());
                    case WITHDRAW, TRANSFER_OUT ->
                            totalNet = totalNet.subtract(tx.getAmount());
                }
            }
        }

        BigDecimal divisor = new BigDecimal(Math.max(txCount, 30));
        BigDecimal avgDailyNet = txCount > 0
                ? totalNet.divide(divisor, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal predictedBalance = currentBalance
                .add(avgDailyNet.multiply(new BigDecimal(days)))
                .setScale(2, RoundingMode.HALF_UP);

        String trend = avgDailyNet.compareTo(BigDecimal.ZERO) > 0 ? "GROWING"
                : avgDailyNet.compareTo(BigDecimal.ZERO) < 0 ? "DECLINING"
                : "STABLE";

        String systemPrompt = """
                You are a financial advisor for Forsa Bank, helping low-income clients in Tunisia.
                Write in simple, friendly, encouraging language. Maximum 3 sentences. No markdown.
                """;

        String userMessage = String.format(
                """
                Account #%d — current balance: %.2f TND.
                Based on %d transactions, the average daily net change is %.2f TND.
                Predicted balance in %d days: %.2f TND. Trend: %s.
                Write a short friendly explanation of this forecast for the client.
                """,
                accountId, currentBalance, txCount, avgDailyNet,
                days, predictedBalance, trend);

        String explanation;
        try {
            explanation = walletAiService.askAI(systemPrompt, userMessage);
        } catch (Exception e) {
            explanation = "Your balance trend is " + trend.toLowerCase()
                    + " based on your recent activity.";
        }

        return new WalletForecastDTO(currentBalance, predictedBalance, days, trend, explanation);
    }

    @Override
    @Transactional
    public AdaptiveInterestResultDTO applyAdaptiveInterest(Long accountId) {
        Account account = findAccount(accountId);

        if (account.getType() != AccountType.INVESTMENT)
            throw new RuntimeException("Adaptive interest only applies to INVESTMENT accounts");
        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked — admin must activate it first");

        Wallet wallet = account.getWallet();
        BigDecimal currentBalance = wallet.getBalance();
        int txCount = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;

        BigDecimal totalDeposits = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.WITHDRAW);

        String systemPrompt = """
                You are a banking AI for Forsa Bank serving low-income clients in Tunisia.
                Decide a fair monthly interest rate for this investment account.
                Rules:
                - Rate must be between 0.5 and 5.0 (percent)
                - Higher balance and more deposits = higher rate (reward loyalty)
                - Frequent withdrawals = lower rate (less stable)
                - Be fair and generous to low-income clients
                Respond ONLY with valid JSON, no markdown, no extra text.
                Format exactly: {"rate": 2.5, "justification": "one sentence"}
                """;

        String userMessage = String.format(
                """
                Account #%d | Type: INVESTMENT
                Current balance: %.2f TND
                Total deposits ever: %.2f TND
                Total withdrawals ever: %.2f TND
                Total transactions: %d
                Decide the monthly interest rate.
                """,
                accountId, currentBalance,
                totalDeposits != null ? totalDeposits : BigDecimal.ZERO,
                totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO,
                txCount);

        double rate = 1.0;
        String justification = "Default 1% rate applied.";

        try {
            String raw = walletAiService.askAI(systemPrompt, userMessage);
            String cleaned = raw.replaceAll("```json|```", "").trim();
            JsonNode node = mapper.readTree(cleaned);
            rate = node.path("rate").asDouble(1.0);
            justification = node.path("justification").asText(justification);
            rate = Math.min(5.0, Math.max(0.5, rate));
        } catch (Exception e) {
            rate = 1.0;
            justification = "Default rate applied (AI unavailable).";
        }

        BigDecimal rateDecimal = BigDecimal.valueOf(rate / 100.0)
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal interest = currentBalance.multiply(rateDecimal)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = currentBalance.add(interest);

        wallet.setBalance(newBalance);
        walletRepo.save(wallet);
        saveTransaction(wallet, interest, TransactionType.INTEREST);
        logActivity(wallet, String.format(
                "Adaptive interest: +%.2f TND at %.2f%% — %s",
                interest, rate, justification));

        return new AdaptiveInterestResultDTO(
                accountId, currentBalance, interest, newBalance, rate, justification);
    }

    @Override
    public AccountTypeAdviceDTO adviseAccountType(Long accountId) {
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();
        BigDecimal currentBalance = wallet.getBalance();
        int txCount = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;

        BigDecimal totalDeposits = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionRepo
                .sumByWalletAndType(wallet.getId(), TransactionType.WITHDRAW);

        String systemPrompt = """
                You are a financial advisor AI for Forsa Bank in Tunisia helping low-income clients.
                Account types:
                - SAVINGS: always active, no interest, for frequent small transactions
                - INVESTMENT: requires admin activation, earns monthly interest, for clients who save consistently
                Respond ONLY with valid JSON, no markdown.
                Format exactly: {"recommendedType": "SAVINGS", "changeAdvised": false, "reasoning": "two sentences max"}
                """;

        String userMessage = String.format(
                """
                Account #%d | Current type: %s
                Balance: %.2f TND
                Total deposits: %.2f TND
                Total withdrawals: %.2f TND
                Transactions: %d
                Should this client switch account types?
                """,
                accountId, account.getType().name(), currentBalance,
                totalDeposits != null ? totalDeposits : BigDecimal.ZERO,
                totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO,
                txCount);

        String recommendedType = account.getType().name();
        boolean changeAdvised = false;
        String reasoning = "Your current account type suits your usage.";

        try {
            String raw = walletAiService.askAI(systemPrompt, userMessage);
            String cleaned = raw.replaceAll("```json|```", "").trim();
            JsonNode node = mapper.readTree(cleaned);
            recommendedType = node.path("recommendedType").asText(recommendedType);
            changeAdvised = node.path("changeAdvised").asBoolean(false);
            reasoning = node.path("reasoning").asText(reasoning);
        } catch (Exception e) {
            // fallback values already set above
        }

        return new AccountTypeAdviceDTO(
                accountId, account.getType().name(),
                recommendedType, changeAdvised, reasoning);
    }
}