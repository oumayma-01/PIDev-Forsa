package org.example.forsapidev.Controllers;

import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create account
    @PostMapping("/create")
    public Account createAccount(@RequestParam Long ownerId,
                                 @RequestParam String type) {
        return accountService.createAccount(ownerId, type);
    }

    // Deposit
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @RequestParam BigDecimal amount) {
        accountService.deposit(id, amount);
        return "Deposit successful";
    }

    // Withdraw
    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @RequestParam BigDecimal amount) {
        accountService.withdraw(id, amount);
        return "Withdrawal successful";
    }

    // Transfer
    @PostMapping("/transfer")
    public String transfer(@RequestParam Long fromWalletId,
                           @RequestParam Long toWalletId,
                           @RequestParam BigDecimal amount) {
        accountService.transfer(fromWalletId, toWalletId, amount);
        return "Transfer successful";
    }

    // Apply Monthly Interest
    @PostMapping("/apply-interest")
    public String applyMonthlyInterest() {
        accountService.applyMonthlyInterest();
        return "Monthly interest applied";
    }

    // Statistics
    @GetMapping("/{id}/statistics")
    public WalletStatistics getStatistics(@PathVariable Long id) {
        return accountService.getStatistics(id);
    }

    // Filter transactions by type
    @GetMapping("/{id}/transactions/filter")
    public List<Transaction> filterTransactions(
            @PathVariable Long id,
            @RequestParam TransactionType type) {
        return accountService.filterTransactions(id, type);
    }

    // Historique des activités
    @GetMapping("/{id}/activities")
    public List<Activity> getActivities(@PathVariable Long id) {
        return accountService.getActivities(id);
    }
}