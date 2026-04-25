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
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // CLIENT creates their own account — ownerId from JWT, not from request
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/create")
    public Account createAccount(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                 @RequestParam String type) {
        return accountService.createAccount(currentUser.getId(), type);
    }

    // CLIENT sees their own accounts — this solves the "forgotten account ID" problem
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/my-accounts")
    public List<Account> getMyAccounts(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return accountService.getAccountsByOwner(currentUser.getId());
    }

    // ADMIN sees all accounts
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // Get single account — any authenticated user (service handles access)
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    // ADMIN only
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return "Account deleted successfully";
    }

    // ADMIN only — block or activate an account
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Account updateAccountStatus(@PathVariable Long id,
                                       @RequestParam String status) {
        return accountService.updateAccountStatus(id, status);
    }

    // ADMIN only — look up accounts by any user ID
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/owner/{ownerId}")
    public List<Account> getAccountsByOwner(@PathVariable Long ownerId) {
        return accountService.getAccountsByOwner(ownerId);
    }

    // CLIENT only — deposit into their own account
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @RequestParam BigDecimal amount,
                          @AuthenticationPrincipal UserDetailsImpl currentUser) {
        accountService.deposit(id, amount, currentUser.getId());
        return "Deposit successful";
    }

    // CLIENT only — withdraw from their own account
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           @RequestParam BigDecimal amount,
                           @AuthenticationPrincipal UserDetailsImpl currentUser) {
        accountService.withdraw(id, amount, currentUser.getId());
        return "Withdrawal successful";
    }

    // CLIENT only — transfer from their own account to any other account
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/transfer")
    public String transfer(@RequestParam Long fromAccountId,
                           @RequestParam Long toAccountId,
                           @RequestParam BigDecimal amount,
                           @AuthenticationPrincipal UserDetailsImpl currentUser) {
        accountService.transfer(fromAccountId, toAccountId, amount, currentUser.getId());
        return "Transfer successful";
    }

    // ADMIN only — manual fixed interest override
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/apply-interest")
    public String applyMonthlyInterest() {
        accountService.applyMonthlyInterest();
        return "Monthly interest applied";
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/statistics")
    public WalletStatisticsDTO getStatistics(@PathVariable Long id) {
        return accountService.getStatistics(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/transactions/filter")
    public List<Transaction> filterTransactions(@PathVariable Long id,
                                                @RequestParam TransactionType type) {
        return accountService.filterTransactions(id, type);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/activities")
    public List<Activity> getActivities(@PathVariable Long id) {
        return accountService.getActivities(id);
    }

    // AI endpoints — CLIENT only
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{id}/forecast")
    public WalletForecastDTO forecastBalance(@PathVariable Long id,
                                             @RequestParam(defaultValue = "30") int days) {
        return accountService.forecastBalance(id, days);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/adaptive-interest")
    public AdaptiveInterestResultDTO applyAdaptiveInterest(@PathVariable Long id) {
        return accountService.applyAdaptiveInterest(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/{id}/account-type-advice")
    public AccountTypeAdviceDTO adviseAccountType(@PathVariable Long id) {
        return accountService.adviseAccountType(id);
    }
}