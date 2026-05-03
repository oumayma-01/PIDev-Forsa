package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.DTO.AccountJsonDTO;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.BankVaultDTO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;
    private final ActivityRepository activityRepo;
    private final UserRepository userRepo;
    private final BankVaultRepository bankVaultRepo;
    private final WalletAiService walletAiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccountServiceImpl(AccountRepository accountRepo,
                              WalletRepository walletRepo,
                              TransactionRepository transactionRepo,
                              ActivityRepository activityRepo,
                              UserRepository userRepo,
                              BankVaultRepository bankVaultRepo,
                              WalletAiService walletAiService) {
        this.accountRepo     = accountRepo;
        this.walletRepo      = walletRepo;
        this.transactionRepo = transactionRepo;
        this.activityRepo    = activityRepo;
        this.userRepo        = userRepo;
        this.bankVaultRepo   = bankVaultRepo;
        this.walletAiService = walletAiService;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Account findAccount(Long accountId) {
        return accountRepo.findByIdWithWalletAndTransactions(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
    }

    private List<Account> mergeAccountsForOwner(long ownerId) {
        Map<Long, Account> merged = new LinkedHashMap<>();
        for (Account a : accountRepo.findByOwner_Id(ownerId)) {
            merged.put(a.getId(), a);
        }
        for (Account a : accountRepo.findByWallet_OwnerId(ownerId)) {
            merged.put(a.getId(), a);
        }
        return new ArrayList<>(merged.values());
    }

    private Wallet findWallet(Long accountId) {
        return findAccount(accountId).getWallet();
    }

    private void logActivity(Wallet wallet, String action) {
        Activity activity = new Activity();
        activity.setAction(action);
        activity.setTimestamp(LocalDateTime.now());
        activity.setWallet(wallet);
        activityRepo.save(activity);
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType(type);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setWallet(wallet);
        transactionRepo.save(tx);
    }

    private BankVault getOrCreateVault() {
        BankVault vault = bankVaultRepo.findById(1L).orElseGet(() -> {
            BankVault newVault = new BankVault(BigDecimal.ZERO);
            return bankVaultRepo.save(newVault);
        });
        if (vault.getTotalFunds() == null) {
            vault.setTotalFunds(BigDecimal.ZERO);
            vault = bankVaultRepo.save(vault);
        }
        return vault;
    }

    private void increaseVault(BigDecimal amount) {
        BankVault vault = getOrCreateVault();
        vault.setTotalFunds(vault.getTotalFunds().add(amount));
        bankVaultRepo.save(vault);
    }

    private void decreaseVault(BigDecimal amount) {
        BankVault vault = getOrCreateVault();
        vault.setTotalFunds(vault.getTotalFunds().subtract(amount));
        bankVaultRepo.save(vault);
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public Account createAccount(Long ownerId, String type) {

        User user = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + ownerId));

        // Vérifier que l'utilisateur est un CLIENT (role_id = 2)
        if (user.getRole() == null || user.getRole().getId() != 2L) {
            throw new RuntimeException("Only clients (role_id=2) can create bank accounts.");
        }

        AccountType requestedType;
        if (type == null) {
            throw new RuntimeException("Account type cannot be null");
        }

        if (type.equalsIgnoreCase("INVESTMENT")) {
            requestedType = AccountType.INVESTMENT;
        } else if (type.equalsIgnoreCase("SAVINGS")) {
            requestedType = AccountType.SAVINGS;
        } else {
            throw new RuntimeException("Invalid account type. Use 'INVESTMENT' or 'SAVINGS'");
        }

        // Un seul compte par type
        List<Account> existingAccounts = getAccountsByOwner(ownerId);

        boolean alreadyExists = existingAccounts.stream()
                .anyMatch(a -> a.getType() == requestedType);

        if (alreadyExists) {
            throw new RuntimeException(
                    "Client already has a " + requestedType + " account. Maximum one account per type."
            );
        }

        // Chaque compte a SA PROPRE wallet
        Wallet wallet = new Wallet();
        wallet.setOwnerId(ownerId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepo.save(wallet);

        Account account = new Account();
        account.setWallet(wallet);
        account.setOwner(user);
        account.setAccountHolderName(user.getUsername());
        account.setType(requestedType);
        if (requestedType == AccountType.SAVINGS) {
            account.setStatus(AccountStatus.ACTIVE);
        } else {
            account.setStatus(AccountStatus.BLOCKED);
        }

        Account saved = accountRepo.save(account);
        logActivity(wallet, "Account created of type: " + requestedType + " (" + saved.getStatus() + ")");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(Long accountId) {
        return findAccount(accountId);
    }

    @Override
    public List<Account> getAccountsByOwner(Long ownerId) {
        return mergeAccountsForOwner(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountJsonDTO> getAccountsByOwnerAsJson(Long ownerId) {
        return AccountJsonDTO.fromList(mergeAccountsForOwner(ownerId), true);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    @Override
    @Transactional
    public Account updateAccountStatus(Long accountId, String status) {
        Account account = findAccount(accountId);
        AccountStatus newStatus = AccountStatus.valueOf(status.toUpperCase());
        account.setStatus(newStatus);
        Account saved = accountRepo.save(account);
        logActivity(account.getWallet(), "Status changed to: " + newStatus);
        return saved;
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();

        if (wallet != null) {
            // 1. Retirer l'argent du BankVault
            decreaseVault(wallet.getBalance());

            // 2. Supprimer TOUTES les activités liées à cette wallet
            List<Activity> walletActivities = activityRepo.findByWallet_Id(wallet.getId());
            if (walletActivities != null && !walletActivities.isEmpty()) {
                activityRepo.deleteAll(walletActivities);
            }

            // 3. Supprimer TOUTES les transactions liées à cette wallet
            if (wallet.getTransactions() != null && !wallet.getTransactions().isEmpty()) {
                transactionRepo.deleteAll(wallet.getTransactions());
            }

            // 4. D'abord dissocier le compte de la wallet (important !)
            account.setWallet(null);
            accountRepo.save(account);

            // 5. Supprimer la wallet
            walletRepo.delete(wallet);
        }

        // 6. Supprimer le compte
        accountRepo.delete(account);

        System.out.println("Account " + accountId + " and its wallet deleted successfully.");
    }

    @Override
    @Transactional
    public void deleteUserWithAccounts(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Account> userAccounts = getAccountsByOwner(userId);

        for (Account account : userAccounts) {
            Wallet wallet = account.getWallet();
            if (wallet != null) {
                decreaseVault(wallet.getBalance());

                List<Activity> walletActivities = activityRepo.findByWallet_Id(wallet.getId());
                if (walletActivities != null && !walletActivities.isEmpty()) {
                    activityRepo.deleteAll(walletActivities);
                }

                if (wallet.getTransactions() != null && !wallet.getTransactions().isEmpty()) {
                    transactionRepo.deleteAll(wallet.getTransactions());
                }

                account.setWallet(null);
                accountRepo.save(account);
                walletRepo.delete(wallet);
            }
            accountRepo.delete(account);
        }

        userRepo.delete(user);
        System.out.println("User " + userId + " and all associated data deleted.");
    }

    // ── OPERATIONS ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Deposit must be positive");

        Account account = findAccount(accountId);
        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked. Please contact an administrator.");

        Wallet wallet = account.getWallet();
        if (wallet == null) throw new RuntimeException("Account has no associated wallet.");
        if (wallet.getBalance() == null) wallet.setBalance(BigDecimal.ZERO);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);
        increaseVault(amount);
        saveTransaction(wallet, amount, TransactionType.DEPOSIT);
        logActivity(wallet, "Deposit of " + amount);
    }

    @Override
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Withdrawal must be positive");

        Account account = findAccount(accountId);
        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked. Please contact an administrator.");

        Wallet wallet = account.getWallet();
        if (wallet == null) throw new RuntimeException("Account has no associated wallet.");
        if (wallet.getBalance() == null) wallet.setBalance(BigDecimal.ZERO);

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);
        decreaseVault(amount);
        saveTransaction(wallet, amount, TransactionType.WITHDRAW);
        logActivity(wallet, "Withdrawal of " + amount);
    }

    @Override
    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId))
            throw new RuntimeException("Cannot transfer to same account");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Transfer must be positive");

        Account from = findAccount(fromAccountId);
        Account to = findAccount(toAccountId);

        if (from.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Source account is blocked.");
        if (to.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Destination account is blocked.");

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

        logActivity(fromWallet, "Transfer sent: " + amount + " to account " + toAccountId);
        logActivity(toWallet, "Transfer received: " + amount + " from account " + fromAccountId);
    }

    @Override
    @Transactional
    public void applyMonthlyInterest() {
        System.out.println("Applying monthly interest to all active INVESTMENT accounts (manual trigger)...");

        for (Account account : accountRepo.findAll()) {
            if (account.getType() == AccountType.INVESTMENT
                    && account.getStatus() == AccountStatus.ACTIVE) {
                Wallet wallet = account.getWallet();
                BigDecimal interest = wallet.getBalance()
                        .multiply(new BigDecimal("0.01"))
                        .setScale(2, RoundingMode.HALF_UP);

                if (interest.compareTo(BigDecimal.ZERO) > 0) {
                    wallet.setBalance(wallet.getBalance().add(interest));
                    walletRepo.save(wallet);
                    decreaseVault(interest); // ✅ bank pays the client — vault decreases
                    saveTransaction(wallet, interest, TransactionType.INTEREST);
                    logActivity(wallet, "Monthly interest applied: " + interest);
                }
            }
        }
    }

    // ── QUERIES ──────────────────────────────────────────────────────────────

    @Override
    public BigDecimal getBalance(Long accountId) {
        return findWallet(accountId).getBalance();
    }

    @Override
    public WalletStatisticsDTO getStatistics(Long accountId) {
        Wallet wallet = findWallet(accountId);
        BigDecimal deposits = transactionRepo.sumByWalletAndType(wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal withdrawals = transactionRepo.sumByWalletAndType(wallet.getId(), TransactionType.WITHDRAW);
        int totalCount = wallet.getTransactions() != null ? wallet.getTransactions().size() : 0;
        return new WalletStatisticsDTO(wallet.getBalance(), deposits, withdrawals, totalCount);
    }

    @Override
    public List<Transaction> filterTransactions(Long accountId, TransactionType type) {
        return findWallet(accountId).getTransactions().stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    @Override
    public List<Activity> getActivities(Long accountId) {
        return activityRepo.findByWallet_Id(findWallet(accountId).getId());
    }

    // ── BANK VAULT ───────────────────────────────────────────────────────────

    @Override
    public BankVaultDTO getBankVault() {
        BankVault vault = getOrCreateVault();
        return new BankVaultDTO(vault.getTotalFunds());
    }

    // ── IA ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public WalletForecastDTO forecastBalance(Long accountId, int days) {
        if (days <= 0) {
            days = 30;
        }
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        BigDecimal netLast30 = BigDecimal.ZERO;
        int recentCount = 0;
        List<Transaction> txs = wallet.getTransactions() != null ? wallet.getTransactions() : List.of();
        for (Transaction t : txs) {
            if (t.getDate() == null || t.getDate().isBefore(cutoff) || t.getAmount() == null) {
                continue;
            }
            recentCount++;
            switch (t.getType()) {
                case DEPOSIT, TRANSFER_IN, INTEREST -> netLast30 = netLast30.add(t.getAmount());
                case WITHDRAW, TRANSFER_OUT -> netLast30 = netLast30.subtract(t.getAmount());
                default -> { }
            }
        }

        BigDecimal dailyDrift = netLast30.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        BigDecimal predicted = balance.add(dailyDrift.multiply(BigDecimal.valueOf(days)))
                .setScale(2, RoundingMode.HALF_UP);
        if (predicted.compareTo(BigDecimal.ZERO) < 0) {
            predicted = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String trend;
        if (predicted.compareTo(balance) > 0) {
            trend = "increasing";
        } else if (predicted.compareTo(balance) < 0) {
            trend = "decreasing";
        } else {
            trend = "stable";
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Projection uses your last 30 days of flows (net ").append(netLast30.setScale(2, RoundingMode.HALF_UP))
                .append("). ");
        if (recentCount == 0) {
            explanation.append("No recent movements in that window — forecast assumes flat balance.");
        } else {
            explanation.append(recentCount).append(" movement(s) counted; daily drift ~ ")
                    .append(dailyDrift.setScale(2, RoundingMode.HALF_UP)).append(".");
        }

        if (walletAiService.isConfigured()) {
            try {
                String ai = walletAiService.askAI(
                        "You help users understand a simple wallet forecast. Reply in 2 short sentences, plain language, no JSON.",
                        "Current balance: " + balance + ". Predicted in " + days + " days: " + predicted
                                + ". Trend label: " + trend + ". Net last 30 days: " + netLast30.setScale(2, RoundingMode.HALF_UP) + ".");
                explanation.append(" ").append(ai.trim());
            } catch (RuntimeException ignored) {
                explanation.append(" (AI explanation unavailable.)");
            }
        }

        return new WalletForecastDTO(balance, predicted, days, trend, explanation.toString().trim());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountTypeAdviceDTO adviseAccountType(Long accountId) {
        Account account = findAccount(accountId);
        String current = account.getType() != null ? account.getType().name() : "UNKNOWN";
        WalletStatisticsDTO st = getStatistics(accountId);
        BigDecimal bal = st.getBalance() != null ? st.getBalance() : BigDecimal.ZERO;
        int n = st.getNumberOfTransactions();

        String recommended;
        boolean change;
        String reasoning;

        if (account.getType() == AccountType.INVESTMENT) {
            recommended = "INVESTMENT";
            if (account.getStatus() == AccountStatus.BLOCKED) {
                change = false;
                reasoning = "Investment wallet is blocked until staff activates it. After activation, it can receive manual monthly interest runs.";
            } else {
                change = false;
                reasoning = "You are already on an investment wallet suited for interest credits when the bank runs monthly interest.";
            }
        } else {
            // SAVINGS
            boolean heavyUse = n >= 8;
            boolean cushion = bal.compareTo(new BigDecimal("500")) >= 0;
            if (heavyUse && cushion) {
                recommended = "INVESTMENT";
                change = true;
                reasoning = "You have steady activity and a solid balance; opening a separate INVESTMENT wallet can separate long-term funds from daily spending (subject to staff activation).";
            } else {
                recommended = "SAVINGS";
                change = false;
                reasoning = n < 5
                        ? "Keep using SAVINGS until you have a clearer pattern of deposits and withdrawals."
                        : "Your usage fits a primary SAVINGS wallet; consider INVESTMENT once balance and activity grow.";
            }
        }

        if (walletAiService.isConfigured()) {
            try {
                String ai = walletAiService.askAI(
                        "You advise on SAVINGS vs INVESTMENT for a demo digital wallet. One short paragraph, agree or refine the recommendation, no JSON.",
                        "Account id " + accountId + ", type " + current + ", balance " + bal + ", tx count " + n
                                + ". Draft recommendation: " + recommended + ", changeAdvised=" + change + ". Reason so far: " + reasoning);
                reasoning = reasoning + " " + ai.trim();
            } catch (RuntimeException ignored) {
                reasoning = reasoning + " (AI add-on unavailable.)";
            }
        }

        return new AccountTypeAdviceDTO(accountId, current, recommended, change, reasoning.trim());
    }
}