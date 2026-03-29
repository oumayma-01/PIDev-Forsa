package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Repositories.*;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;
    private final ActivityRepository activityRepo;

    public AccountServiceImpl(AccountRepository accountRepo,
                              WalletRepository walletRepo,
                              TransactionRepository transactionRepo,
                              ActivityRepository activityRepo) {
        this.accountRepo = accountRepo;
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
        this.activityRepo = activityRepo;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    // Get account or throw — used everywhere
    private Account findAccount(Long accountId) {
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
    }

    // Get wallet from account
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
        tx.setWallet(wallet);
        transactionRepo.save(tx);
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Account createAccount(Long ownerId, String type) {
        Wallet wallet = new Wallet();
        wallet.setOwnerId(ownerId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepo.save(wallet);

        Account account = new Account();
        account.setWallet(wallet);

        if (type.equalsIgnoreCase("INVESTMENT")) {
            account.setType(AccountType.INVESTMENT);
            account.setStatus(AccountStatus.BLOCKED);
        } else {
            account.setType(AccountType.SAVINGS);
            account.setStatus(AccountStatus.ACTIVE);
        }

        Account saved = accountRepo.save(account);
        logActivity(wallet, "Account created of type: " + type);
        return saved;
    }

    @Override
    public Account getAccount(Long accountId) {
        return findAccount(accountId);
    }

    @Override
    public List<Account> getAccountsByOwner(Long ownerId) {
        return accountRepo.findAll()
                .stream()
                .filter(a -> a.getWallet() != null &&
                        ownerId.equals(a.getWallet().getOwnerId()))
                .toList();
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

        // Delete in order to avoid FK constraint violations
        activityRepo.deleteAll(activityRepo.findByWallet_Id(wallet.getId()));
        transactionRepo.deleteAll(
                transactionRepo.findAll()
                        .stream()
                        .filter(t -> t.getWallet().getId().equals(wallet.getId()))
                        .toList()
        );
        accountRepo.delete(account);
        walletRepo.delete(wallet);
    }

    // ── OPERATIONS ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Deposit amount must be positive");

        Account account = findAccount(accountId);

        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked");

        Wallet wallet = account.getWallet();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);

        saveTransaction(wallet, amount, TransactionType.DEPOSIT);
        logActivity(wallet, "Deposit of " + amount);
    }

    @Override
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Withdrawal amount must be positive");

        Account account = findAccount(accountId);

        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked");

        Wallet wallet = account.getWallet();

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);

        saveTransaction(wallet, amount, TransactionType.WITHDRAW);
        logActivity(wallet, "Withdrawal of " + amount);
    }

    @Override
    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Transfer amount must be positive");

        if (fromAccountId.equals(toAccountId))
            throw new RuntimeException("Cannot transfer to the same account");

        Account fromAccount = findAccount(fromAccountId);
        Account toAccount   = findAccount(toAccountId);

        if (fromAccount.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Source account is blocked");

        if (toAccount.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Destination account is blocked");

        Wallet from = fromAccount.getWallet();
        Wallet to   = toAccount.getWallet();

        if (from.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        walletRepo.save(from);
        walletRepo.save(to);

        saveTransaction(from, amount, TransactionType.TRANSFER_OUT);
        saveTransaction(to,   amount, TransactionType.TRANSFER_IN);

        logActivity(from, "Transfer sent: " + amount + " to account " + toAccountId);
        logActivity(to,   "Transfer received: " + amount + " from account " + fromAccountId);
    }

    @Override
    @Transactional
    public void applyMonthlyInterest() {
        List<Account> accounts = accountRepo.findAll();
        for (Account account : accounts) {
            if (account.getType() == AccountType.INVESTMENT
                    && account.getStatus() == AccountStatus.ACTIVE) {

                Wallet wallet = account.getWallet();
                BigDecimal interest = wallet.getBalance()
                        .multiply(new BigDecimal("0.001"));

                wallet.setBalance(wallet.getBalance().add(interest));
                walletRepo.save(wallet);

                saveTransaction(wallet, interest, TransactionType.INTEREST);
                logActivity(wallet, "Monthly interest applied: " + interest);
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
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();

        BigDecimal deposits    = transactionRepo.sumByWalletAndType(wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal withdrawals = transactionRepo.sumByWalletAndType(wallet.getId(), TransactionType.WITHDRAW);
        int totalCount = wallet.getTransactions() != null ? wallet.getTransactions().size() : 0;

        return new WalletStatisticsDTO(wallet.getBalance(), deposits, withdrawals, totalCount);
    }

    @Override
    public List<Transaction> filterTransactions(Long accountId, TransactionType type) {
        Wallet wallet = findWallet(accountId);
        return wallet.getTransactions()
                .stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    @Override
    public List<Activity> getActivities(Long accountId) {
        Wallet wallet = findWallet(accountId);
        return activityRepo.findByWallet_Id(wallet.getId())
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }
}