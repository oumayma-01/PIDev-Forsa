package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.databind.JsonNode;
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

    private final AccountRepository  accountRepo;
    private final WalletRepository   walletRepo;
    private final TransactionRepository transactionRepo;
    private final ActivityRepository activityRepo;
    private final WalletAiService    walletAiService;

    private final ObjectMapper mapper = new ObjectMapper();

    public AccountServiceImpl(AccountRepository accountRepo,
                              WalletRepository walletRepo,
                              TransactionRepository transactionRepo,
                              ActivityRepository activityRepo,
                              WalletAiService walletAiService) {
        this.accountRepo     = accountRepo;
        this.walletRepo      = walletRepo;
        this.transactionRepo = transactionRepo;
        this.activityRepo    = activityRepo;
        this.walletAiService = walletAiService;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Account findAccount(Long accountId) {
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found with id: " + accountId));
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

    private void saveTransaction(Wallet wallet,
                                 BigDecimal amount,
                                 TransactionType type) {
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
    public Account createAccount(Long ownerId, String type, String holderName) {
        Wallet wallet = new Wallet();
        wallet.setOwnerId(ownerId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepo.save(wallet);

        Account account = new Account();
        account.setWallet(wallet);
        account.setAccountHolderName(holderName); // ← nouveau

        if (type.equalsIgnoreCase("INVESTMENT")) {
            account.setType(AccountType.INVESTMENT);
            account.setStatus(AccountStatus.BLOCKED);
        } else {
            account.setType(AccountType.SAVINGS);
            account.setStatus(AccountStatus.ACTIVE);
        }

        Account saved = accountRepo.save(account);
        logActivity(wallet, "Account created of type: " + type
                + " for holder: " + holderName);
        return saved;
    }

    @Override
    public Account getAccount(Long accountId) {
        return findAccount(accountId);
    }

    @Override
    public List<Account> getAccountsByOwner(Long ownerId) {
        return accountRepo.findAll().stream()
                .filter(a -> a.getWallet() != null
                        && ownerId.equals(a.getWallet().getOwnerId()))
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
        logActivity(account.getWallet(),
                "Status changed to: " + status.toUpperCase());
        return saved;
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = findAccount(accountId);
        Wallet wallet = account.getWallet();
        activityRepo.deleteAll(
                activityRepo.findByWallet_Id(wallet.getId()));
        transactionRepo.deleteAll(
                transactionRepo.findAll().stream()
                        .filter(t -> t.getWallet().getId()
                                .equals(wallet.getId()))
                        .toList());
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
    public void transfer(Long fromAccountId,
                         Long toAccountId,
                         BigDecimal amount) {
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
        logActivity(from, "Transfer sent: "     + amount + " to account "   + toAccountId);
        logActivity(to,   "Transfer received: " + amount + " from account " + fromAccountId);
    }

    @Override
    @Transactional
    public void applyMonthlyInterest() {
        for (Account account : accountRepo.findAll()) {
            if (account.getType()   == AccountType.INVESTMENT
                    && account.getStatus() == AccountStatus.ACTIVE) {
                Wallet wallet   = account.getWallet();
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
        Wallet  wallet  = account.getWallet();

        BigDecimal deposits    = transactionRepo.sumByWalletAndType(
                wallet.getId(), TransactionType.DEPOSIT);
        BigDecimal withdrawals = transactionRepo.sumByWalletAndType(
                wallet.getId(), TransactionType.WITHDRAW);
        int totalCount = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;

        return new WalletStatisticsDTO(
                wallet.getBalance(), deposits, withdrawals, totalCount);
    }

    @Override
    public List<Transaction> filterTransactions(Long accountId,
                                                TransactionType type) {
        Wallet wallet = findWallet(accountId);
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    @Override
    public List<Activity> getActivities(Long accountId) {
        Wallet wallet = findWallet(accountId);
        return activityRepo.findByWallet_Id(wallet.getId()).stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    // ── IA ───────────────────────────────────────────────────────────────────

    @Override
    public WalletForecastDTO forecastBalance(Long accountId, int days) {
        Account account = findAccount(accountId);
        Wallet  wallet  = account.getWallet();

        // Résumé des 20 dernières transactions
        StringBuilder txSummary = new StringBuilder();
        if (wallet.getTransactions() != null) {
            wallet.getTransactions().stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .limit(20)
                    .forEach(t -> txSummary
                            .append(t.getDate().toLocalDate())
                            .append(" | ").append(t.getType())
                            .append(" | ").append(t.getAmount())
                            .append("\n"));
        }

        String systemPrompt = """
                Tu es un analyste financier expert en microfinance.
                Réponds UNIQUEMENT avec un objet JSON valide, sans texte avant ni après.
                Format attendu :
                {
                  "predictedBalance": <nombre décimal>,
                  "trend": "<HAUSSE|BAISSE|STABLE>",
                  "explanation": "<explication courte en français, max 2 phrases>"
                }
                """;

        String userMessage = String.format("""
                Solde actuel : %s TND
                Horizon de prévision : %d jours
                Historique des transactions récentes (date | type | montant) :
                %s
                En te basant sur ces données, prévis le solde probable dans %d jours.
                """, wallet.getBalance(), days,
                txSummary.isEmpty() ? "Aucune transaction enregistrée." : txSummary,
                days);

        String aiResponse = walletAiService.askAI(systemPrompt, userMessage);

        try {
            // Nettoyer la réponse si elle contient des backticks markdown
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode node = mapper.readTree(cleaned);

            BigDecimal predicted  = new BigDecimal(
                    node.path("predictedBalance").asText("0"));
            String trend          = node.path("trend").asText("STABLE");
            String explanation    = node.path("explanation").asText("");

            return new WalletForecastDTO(
                    wallet.getBalance(), predicted, days, trend, explanation);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible de parser la réponse IA : " + aiResponse, e);
        }
    }

    @Override
    @Transactional
    public AdaptiveInterestResultDTO applyAdaptiveInterest(Long accountId) {
        Account account = findAccount(accountId);

        if (account.getType() != AccountType.INVESTMENT)
            throw new RuntimeException(
                    "Seuls les comptes INVESTMENT bénéficient d'intérêts adaptatifs");
        if (account.getStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Compte bloqué");

        Wallet wallet = account.getWallet();

        long totalTx = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;
        long depositCount = wallet.getTransactions() != null
                ? wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .count()
                : 0;
        long withdrawCount = wallet.getTransactions() != null
                ? wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW)
                .count()
                : 0;

        String systemPrompt = """
                Tu es un expert en produits d'épargne microfinance.
                Réponds UNIQUEMENT avec un objet JSON valide, sans texte avant ni après.
                Format attendu :
                {
                  "rate": <nombre décimal entre 0.0005 et 0.005>,
                  "justification": "<raison courte en français, max 2 phrases>"
                }
                Règle : un client avec beaucoup de dépôts et peu de retraits mérite un taux élevé.
                Un solde élevé et stable est aussi récompensé.
                """;

        String userMessage = String.format("""
                Profil du compte INVESTMENT :
                - Solde actuel : %s TND
                - Nombre total de transactions : %d
                - Nombre de dépôts : %d
                - Nombre de retraits : %d
                Calcule un taux d'intérêt mensuel personnalisé entre 0.0005 (0.05%%) et 0.005 (0.5%%).
                """, wallet.getBalance(), totalTx, depositCount, withdrawCount);

        String aiResponse = walletAiService.askAI(systemPrompt, userMessage);

        try {
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode node         = mapper.readTree(cleaned);
            double rate           = node.path("rate").asDouble(0.001);
            String justification  = node.path("justification").asText("");

            // Sécurité : on force la plage autorisée
            rate = Math.max(0.0005, Math.min(0.005, rate));

            BigDecimal previousBalance = wallet.getBalance();
            BigDecimal interest = previousBalance
                    .multiply(BigDecimal.valueOf(rate))
                    .setScale(4, RoundingMode.HALF_UP);

            wallet.setBalance(previousBalance.add(interest));
            walletRepo.save(wallet);
            saveTransaction(wallet, interest, TransactionType.INTEREST);
            logActivity(wallet,
                    "Intérêt adaptatif appliqué — taux: " + rate
                            + " | montant: " + interest
                            + " | " + justification);

            return new AdaptiveInterestResultDTO(
                    accountId, previousBalance, interest,
                    wallet.getBalance(), rate, justification);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible de parser la réponse IA : " + aiResponse, e);
        }
    }

    @Override
    public AccountTypeAdviceDTO adviseAccountType(Long accountId) {
        Account account = findAccount(accountId);
        Wallet  wallet  = account.getWallet();

        long depositCount = wallet.getTransactions() != null
                ? wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .count()
                : 0;
        long withdrawCount = wallet.getTransactions() != null
                ? wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW)
                .count()
                : 0;
        long totalTx = wallet.getTransactions() != null
                ? wallet.getTransactions().size() : 0;

        String systemPrompt = """
                Tu es un conseiller financier spécialisé en microfinance.
                Réponds UNIQUEMENT avec un objet JSON valide, sans texte avant ni après.
                Format attendu :
                {
                  "recommendedType": "<SAVINGS|INVESTMENT>",
                  "changeAdvised": <true|false>,
                  "reasoning": "<explication courte en français, max 2 phrases>"
                }
                Règles :
                - INVESTMENT : client qui épargne sur le long terme, peu de retraits, solde stable ou croissant.
                - SAVINGS : client qui fait des retraits fréquents, utilise son argent au quotidien.
                """;

        String userMessage = String.format("""
                Profil du compte actuel : %s
                - Solde : %s TND
                - Total transactions : %d
                - Dépôts effectués : %d
                - Retraits effectués : %d
                Ce profil correspond-il au bon type de compte ?
                Conseille le type optimal et indique si un changement est recommandé.
                """, account.getType(), wallet.getBalance(),
                totalTx, depositCount, withdrawCount);

        String aiResponse = walletAiService.askAI(systemPrompt, userMessage);

        try {
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode node        = mapper.readTree(cleaned);
            String recommended   = node.path("recommendedType")
                    .asText(account.getType().name());
            boolean changeAdvised = node.path("changeAdvised").asBoolean(false);
            String reasoning     = node.path("reasoning").asText("");

            return new AccountTypeAdviceDTO(
                    accountId, account.getType().name(),
                    recommended, changeAdvised, reasoning);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible de parser la réponse IA : " + aiResponse, e);
        }
    }
}