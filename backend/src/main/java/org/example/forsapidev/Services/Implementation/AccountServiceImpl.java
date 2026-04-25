package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.AdaptiveInterestResultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Repositories.*;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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

    private Account findAccount(Long accountId) {
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
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
        tx.setWallet(wallet);
        transactionRepo.save(tx);
    }

    @Transactional
    @Override
    public Account createAccount(Long ownerId, String type) {
        Wallet wallet = new Wallet();
        wallet.setOwnerId(ownerId);
        wallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepo.save(wallet);

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
        return accountRepo.findAll().stream()
                .filter(a -> a.getWallet() != null && ownerId.equals(a.getWallet().getOwnerId()))
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
        activityRepo.deleteAll(activityRepo.findByWallet_Id(wallet.getId()));
        transactionRepo.deleteAll(wallet.getTransactions());
        accountRepo.delete(account);
        walletRepo.delete(wallet);
    }

    @Override
    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Deposit must be positive");

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
            throw new RuntimeException("Withdrawal must be positive");

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
        if (fromAccountId.equals(toAccountId)) throw new RuntimeException("Cannot transfer to same account");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Transfer must be positive");

        Account from = findAccount(fromAccountId);
        Account to = findAccount(toAccountId);

        if (from.getStatus() == AccountStatus.BLOCKED || to.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account is blocked");

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
        for (Account account : accountRepo.findAll()) {
            if (account.getType() == AccountType.INVESTMENT
                    && account.getStatus() == AccountStatus.ACTIVE) {
                Wallet wallet = account.getWallet();
                BigDecimal interest = wallet.getBalance().multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
                wallet.setBalance(wallet.getBalance().add(interest));
                walletRepo.save(wallet);
            }
        }
    }

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

    @Override
    public WalletForecastDTO forecastBalance(Long accountId, int days) {
        return null;
    }

    @Override
    public AdaptiveInterestResultDTO applyAdaptiveInterest(Long accountId) {
        return null;
    }

    @Override
    public AccountTypeAdviceDTO adviseAccountType(Long accountId) {
        return null;
    }
}