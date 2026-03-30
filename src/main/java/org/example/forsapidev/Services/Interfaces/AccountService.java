package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.AdaptiveInterestResultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // ── CRUD ─────────────────────────────────────────────────────────────────
    Account createAccount(Long ownerId, String type, String holderName);    Account getAccount(Long accountId);
    List<Account> getAccountsByOwner(Long ownerId);
    List<Account> getAllAccounts();
    Account updateAccountStatus(Long accountId, String status);
    void deleteAccount(Long accountId);

    // ── OPERATIONS ───────────────────────────────────────────────────────────
    void deposit(Long accountId, BigDecimal amount);
    void withdraw(Long accountId, BigDecimal amount);
    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);
    void applyMonthlyInterest();

    // ── QUERIES ──────────────────────────────────────────────────────────────
    BigDecimal getBalance(Long accountId);
    WalletStatisticsDTO getStatistics(Long accountId);
    List<Transaction> filterTransactions(Long accountId, TransactionType type);
    List<Activity> getActivities(Long accountId);

    // ── IA ───────────────────────────────────────────────────────────────────
    WalletForecastDTO forecastBalance(Long accountId, int days);
    AdaptiveInterestResultDTO applyAdaptiveInterest(Long accountId);
    AccountTypeAdviceDTO adviseAccountType(Long accountId);
}