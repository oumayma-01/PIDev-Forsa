package org.example.forsapidev.Controllers;

import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping("/create")
    public Account createAccount(@RequestParam Long ownerId,
                                 @RequestParam String type) {
        return accountService.createAccount(ownerId, type);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/owner/{ownerId}")
    public List<Account> getAccountsByOwner(@PathVariable Long ownerId) {
        return accountService.getAccountsByOwner(ownerId);
    }

    @PutMapping("/{id}/status")
    public Account updateStatus(@PathVariable Long id,
                                @RequestParam String status) {
        return accountService.updateAccountStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return "Account deleted successfully";
    }

    // ── OPERATIONS ───────────────────────────────────────────────────────────

    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @RequestParam BigDecimal amount) {
        accountService.deposit(id, amount);
        return "Deposit successful";
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @RequestParam BigDecimal amount) {
        accountService.withdraw(id, amount);
        return "Withdrawal successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam("fromWalletId") Long fromAccountId,
                           @RequestParam("toWalletId") Long toAccountId,
                           @RequestParam BigDecimal amount) {
        accountService.transfer(fromAccountId, toAccountId, amount);
        return "Transfer successful";
    }

    @PostMapping("/apply-interest")
    public String applyMonthlyInterest() {
        accountService.applyMonthlyInterest();
        return "Monthly interest applied";
    }

    // ── QUERIES ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/balance")
    public BigDecimal getBalance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }

    @GetMapping("/{id}/statistics")
    public WalletStatisticsDTO getStatistics(@PathVariable Long id) {
        return accountService.getStatistics(id);
    }

    @GetMapping("/{id}/transactions/filter")
    public List<Transaction> filterTransactions(@PathVariable Long id,
                                                @RequestParam TransactionType type) {
        return accountService.filterTransactions(id, type);
    }

    @GetMapping("/{id}/activities")
    public List<Activity> getActivities(@PathVariable Long id) {
        return accountService.getActivities(id);
    }
}