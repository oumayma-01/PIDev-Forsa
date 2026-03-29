package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // CRUD
    Account createAccount(Long ownerId, String type);
    Account getAccount(Long accountId);
    List<Account> getAccountsByOwner(Long ownerId);
    List<Account> getAllAccounts();
    Account updateAccountStatus(Long accountId, String status);
    void deleteAccount(Long accountId);

    // Operations
    void deposit(Long accountId, BigDecimal amount);
    void withdraw(Long accountId, BigDecimal amount);
    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);
    void applyMonthlyInterest();

    // Queries
    BigDecimal getBalance(Long accountId);
    WalletStatisticsDTO getStatistics(Long accountId);
    List<Transaction> filterTransactions(Long accountId, TransactionType type);
    List<Activity> getActivities(Long accountId);
}