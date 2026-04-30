package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.AdaptiveInterestResultDTO;
import org.example.forsapidev.DTO.BankVaultDTO;
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

    // ── CLIENT endpoints ─────────────────────────────────────────────────────

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/create")
    public org.springframework.http.ResponseEntity<?> createAccount(@RequestParam("ownerId") Long ownerId,
                                                                    @RequestParam("type") String type) {
        try {
            return org.springframework.http.ResponseEntity.ok(accountService.createAccount(ownerId, type));
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_CREATE: " + e.getMessage()));
        }
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/owner/{ownerId}")
    public List<Account> getAccountsByOwner(@PathVariable Long ownerId) {
        return accountService.getAccountsByOwner(ownerId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/deposit")
    public org.springframework.http.ResponseEntity<?> deposit(@PathVariable("id") Long id,
                                                              @RequestParam("amount") BigDecimal amount) {
        try {
            accountService.deposit(id, amount);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Deposit successful"));
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_DEPOSIT: " + e.getMessage()));
        }
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/withdraw")
    public org.springframework.http.ResponseEntity<?> withdraw(@PathVariable("id") Long id,
                           @RequestParam("amount") BigDecimal amount) {
        try {
            accountService.withdraw(id, amount);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Withdrawal successful"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_WITHDRAW: " + e.getMessage()));
        }
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/transfer")
    public org.springframework.http.ResponseEntity<?> transfer(@RequestParam("fromAccountId") Long fromAccountId,
                           @RequestParam("toAccountId") Long toAccountId,
                           @RequestParam("amount") BigDecimal amount) {
        try {
            accountService.transfer(fromAccountId, toAccountId, amount);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Transfer successful"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_TRANSFER: " + e.getMessage()));
        }
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/statistics")
    public WalletStatisticsDTO getStatistics(@PathVariable Long id) {
        return accountService.getStatistics(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/transactions/filter")
    public List<Transaction> filterTransactions(
            @PathVariable("id") Long id,
            @RequestParam("type") TransactionType type) {
        return accountService.filterTransactions(id, type);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/activities")
    public List<Activity> getActivities(@PathVariable Long id) {
        return accountService.getActivities(id);
    }

    // ── AGENT + ADMIN endpoints ───────────────────────────────────────────────

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @GetMapping("/vault")
    public BankVaultDTO getBankVault() {
        return accountService.getBankVault();
    }

    // ── ADMIN only endpoints ──────────────────────────────────────────────────

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        try {
            accountService.deleteAccount(id);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_DELETE: " + e.getMessage()));
        }
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Account updateAccountStatus(@PathVariable("id") Long id,
                                       @RequestParam("status") String status) {
        return accountService.updateAccountStatus(id, status);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/apply-interest")
    public org.springframework.http.ResponseEntity<?> applyMonthlyInterest() {
        try {
            accountService.applyMonthlyInterest();
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Monthly interest applied"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "ERROR_INTEREST: " + e.getMessage()));
        }
    }

    // ── AI endpoints ──────────────────────────────────────────────────────────

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/forecast")
    public WalletForecastDTO forecastBalance(
            @PathVariable("id") Long id,
            @RequestParam(value = "days", defaultValue = "30") int days) {
        return accountService.forecastBalance(id, days);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/adaptive-interest")
    public AdaptiveInterestResultDTO applyAdaptiveInterest(@PathVariable Long id) {
        return accountService.applyAdaptiveInterest(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/account-type-advice")
    public AccountTypeAdviceDTO adviseAccountType(@PathVariable Long id) {
        return accountService.adviseAccountType(id);
    }
}