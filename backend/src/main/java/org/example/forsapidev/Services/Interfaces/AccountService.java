package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.AccountJsonDTO;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.BankVaultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    @Transactional
    Account createAccount(Long ownerId, String type);

    Account getAccount(Long accountId);

    List<Account> getAccountsByOwner(Long ownerId);

    /** Same data as {@link #getAccountsByOwner(Long)} but JSON-safe and loaded in one read transaction (transactions included). */
    List<AccountJsonDTO> getAccountsByOwnerAsJson(Long ownerId);

    List<Account> getAllAccounts();

    Account updateAccountStatus(Long accountId, String status);

    void deleteAccount(Long accountId);

    void deleteUserWithAccounts(Long userId);

    void deposit(Long accountId, BigDecimal amount);

    void withdraw(Long accountId, BigDecimal amount);

    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);

    void applyMonthlyInterest();

    BigDecimal getBalance(Long accountId);

    WalletStatisticsDTO getStatistics(Long accountId);

    List<Transaction> filterTransactions(Long accountId, TransactionType type);

    List<Activity> getActivities(Long accountId);

    BankVaultDTO getBankVault();

    WalletForecastDTO forecastBalance(Long accountId, int days);

    AccountTypeAdviceDTO adviseAccountType(Long accountId);
}