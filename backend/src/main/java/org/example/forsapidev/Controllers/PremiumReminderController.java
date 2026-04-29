package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Services.Implementation.PremiumReminderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/premium-reminder")
public class PremiumReminderController {

    private final PremiumReminderService premiumReminderService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-upcoming-reminders")
    public String sendUpcomingReminders() {
        premiumReminderService.sendConsolidatedReminders();
        return "Consolidated payment reminders sent successfully!";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/mark-overdue")
    public String markOverduePayments() {
        premiumReminderService.markNewOverduePayments();
        return "Overdue payments marked and processed successfully!";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-full-check")
    public String runFullCheck() {
        premiumReminderService.checkAndProcessPayments();
        return "Full consolidated reminder check completed successfully!";
    }
}
