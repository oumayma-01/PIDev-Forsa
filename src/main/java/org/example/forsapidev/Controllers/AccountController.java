package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/accounts")
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

    // Statistics
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