package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.AdaptiveInterestResultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Create account - accessible to all authenticated users
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/create")
    public Account createAccount(@RequestParam Long ownerId,
                                 @RequestParam String type) {
        return accountService.createAccount(ownerId, type);
    }

    // Get all accounts - ADMIN ONLY
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // Get single account
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    // Delete account - ADMIN ONLY
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return "Account deleted successfully";
    }

    // Update account status - ADMIN ONLY
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Account updateAccountStatus(@PathVariable Long id,
                                       @RequestParam String status) {
        return accountService.updateAccountStatus(id, status);
    }

    // Get accounts by owner
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/owner/{ownerId}")
    public List<Account> getAccountsByOwner(@PathVariable Long ownerId) {
        return accountService.getAccountsByOwner(ownerId);
    }
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @RequestParam BigDecimal amount) {
        accountService.deposit(id, amount);
        return "Deposit successful";
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @RequestParam BigDecimal amount) {
        accountService.withdraw(id, amount);
        return "Withdrawal successful";
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/transfer")
    public String transfer(@RequestParam Long fromAccountId,
                           @RequestParam Long toAccountId,
                           @RequestParam BigDecimal amount) {
        accountService.transfer(fromAccountId, toAccountId, amount);
        return "Transfer successful";
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/apply-interest")
    public String applyMonthlyInterest() {
        accountService.applyMonthlyInterest();
        return "Monthly interest applied";
    }

    // Statistics
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/statistics")
    public WalletStatisticsDTO getStatistics(@PathVariable Long id) {
        return accountService.getStatistics(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/transactions/filter")
    public List<Transaction> filterTransactions(
            @PathVariable Long id,
            @RequestParam TransactionType type) {
        return accountService.filterTransactions(id, type);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/activities")
    public List<Activity> getActivities(@PathVariable Long id) {
        return accountService.getActivities(id);
    }

    // ── IA ───────────────────────────────────────────────────────────────────

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/forecast")
    public WalletForecastDTO forecastBalance(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        return accountService.forecastBalance(id, days);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/adaptive-interest")
    public AdaptiveInterestResultDTO applyAdaptiveInterest(
            @PathVariable Long id) {
        return accountService.applyAdaptiveInterest(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/account-type-advice")
    public AccountTypeAdviceDTO adviseAccountType(@PathVariable Long id) {
        return accountService.adviseAccountType(id);
    }
}