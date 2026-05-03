package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.AccountJsonDTO;
import org.example.forsapidev.DTO.AccountTypeAdviceDTO;
import org.example.forsapidev.DTO.BankVaultDTO;
import org.example.forsapidev.DTO.WalletForecastDTO;
import org.example.forsapidev.DTO.WalletStatisticsDTO;
import org.example.forsapidev.Services.Interfaces.AccountService;
import org.example.forsapidev.entities.WalletManagement.Activity;
import org.example.forsapidev.entities.WalletManagement.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    public AccountJsonDTO createAccount(@RequestParam Long ownerId,
                                        @RequestParam String type) {
        return AccountJsonDTO.from(accountService.createAccount(ownerId, type));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public AccountJsonDTO getAccount(@PathVariable Long id) {
        return AccountJsonDTO.from(accountService.getAccount(id));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/owner/{ownerId}")
    public List<AccountJsonDTO> getAccountsByOwner(@PathVariable Long ownerId) {
        return accountService.getAccountsByOwnerAsJson(ownerId);
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
    public List<AccountJsonDTO.TransactionJsonDTO> filterTransactions(
            @PathVariable Long id,
            @RequestParam TransactionType type) {
        return accountService.filterTransactions(id, type).stream()
                .map(AccountJsonDTO.TransactionJsonDTO::from)
                .collect(Collectors.toList());
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
    public List<AccountJsonDTO> getAllAccounts() {
        return AccountJsonDTO.fromList(accountService.getAllAccounts(), false);
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
    public AccountJsonDTO updateAccountStatus(@PathVariable Long id,
                                              @RequestParam String status) {
        return AccountJsonDTO.from(accountService.updateAccountStatus(id, status));
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
    @GetMapping("/{id}/account-type-advice")
    public AccountTypeAdviceDTO adviseAccountType(@PathVariable Long id) {
        return accountService.adviseAccountType(id);
    }
}