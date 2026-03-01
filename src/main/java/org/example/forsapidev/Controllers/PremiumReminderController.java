package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Services.Implementation.PremiumReminderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/premium-reminder")
public class PremiumReminderController {

    private final PremiumReminderService premiumReminderService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-upcoming-reminders")
    public String sendUpcomingReminders() {
        premiumReminderService.sendUpcomingPaymentReminders();
        return "Upcoming payment reminders sent successfully!";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/mark-overdue")
    public String markOverduePayments() {
        premiumReminderService.markOverduePayments();
        return "Overdue payments marked successfully!";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-full-check")
    public String runFullCheck() {
        premiumReminderService.checkAndSendReminders();
        return "Full reminder check completed successfully!";
    }
}
