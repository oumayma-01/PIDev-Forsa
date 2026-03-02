package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.AccountRepository;
import org.example.forsapidev.Repositories.ActivityRepository;
import org.example.forsapidev.Repositories.TransactionRepository;
import org.example.forsapidev.Repositories.WalletRepository;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.AccountStatus;
import org.example.forsapidev.entities.WalletManagement.AccountType;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.example.forsapidev.entities.WalletManagement.Wallet;
import org.example.forsapidev.entities.WalletManagement.WalletStatistics;
import org.springframework.stereotype.Service;

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

    // 1️⃣ Create Account
    @Override
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

        Account savedAccount = accountRepo.save(account);
        logActivity(wallet, "Account created of type: " + type);
        return savedAccount;
    }

    // 2️⃣ Deposit
    @Override
    public void deposit(Long accountId, BigDecimal amount) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Wallet wallet = account.getWallet();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType(TransactionType.DEPOSIT);
        tx.setWallet(wallet);
        transactionRepo.save(tx);

        logActivity(wallet, "Deposit of " + amount);
    }

    // 3️⃣ Withdraw
    @Override
    public void withdraw(Long accountId, BigDecimal amount) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Wallet wallet = account.getWallet();

        if (account.getType() == AccountType.INVESTMENT &&
                account.getStatus() == AccountStatus.BLOCKED) {
            throw new RuntimeException("Investment account is blocked");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        tx.setType(TransactionType.WITHDRAW);
        tx.setWallet(wallet);
        transactionRepo.save(tx);

        logActivity(wallet, "Withdrawal of " + amount);
    }

    // 4️⃣ Monthly Interest (0.1%)
    @Override
    public void applyMonthlyInterest() {

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {
            if (account.getType() == AccountType.INVESTMENT) {

                Wallet wallet = account.getWallet();
                BigDecimal interest = wallet.getBalance()
                        .multiply(new BigDecimal("0.001"));

                wallet.setBalance(wallet.getBalance().add(interest));
                walletRepo.save(wallet);

                Transaction tx = new Transaction();
                tx.setAmount(interest);
                tx.setDate(LocalDateTime.now());
                tx.setType(TransactionType.INTEREST);
                tx.setWallet(wallet);
                transactionRepo.save(tx);

                logActivity(wallet, "Monthly interest applied: " + interest);
            }
        }
    }

    // 5️⃣ Transfer
    @Override
    public void transfer(Long fromWalletId, Long toWalletId, BigDecimal amount) {

        Wallet from = walletRepo.findById(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found"));
        Wallet to = walletRepo.findById(toWalletId)
                .orElseThrow(() -> new RuntimeException("Destination wallet not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        walletRepo.save(from);
        walletRepo.save(to);

        logActivity(from, "Transfer sent: " + amount + " to wallet " + toWalletId);
        logActivity(to, "Transfer received: " + amount + " from wallet " + fromWalletId);
    }

    // 6️⃣ Statistics
    @Override
    public WalletStatistics getStatistics(Long walletId) {

        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;

        for (Transaction t : wallet.getTransactions()) {
            if (t.getType() == TransactionType.DEPOSIT) {
                deposits = deposits.add(t.getAmount());
            } else if (t.getType() == TransactionType.WITHDRAW) {
                withdrawals = withdrawals.add(t.getAmount());
            }
        }

        return new WalletStatistics(
                wallet.getBalance(),
                deposits,
                withdrawals,
                wallet.getTransactions().size()
        );
    }

    // 7️⃣ Filter Transactions
    @Override
    public List<Transaction> filterTransactions(Long walletId, TransactionType type) {

        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return wallet.getTransactions()
                .stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    // 8️⃣ Activities
    @Override
    public List<Activity> getActivities(Long walletId) {
        return activityRepo.findByWallet_Id(walletId)
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    // 🔧 Private helper
    private void logActivity(Wallet wallet, String action) {
        Activity activity = new Activity();
        activity.setAction(action);
        activity.setTimestamp(LocalDateTime.now()); // ✅ LocalDateTime
        activity.setWallet(wallet);
        activityRepo.save(activity);
    }
}