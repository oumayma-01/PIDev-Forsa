package org.example.forsapidev.Controllers;

import org.example.forsapidev.Services.Implementation.PremiumReminderService;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/premium-reminder")
public class PremiumReminderController {

    private final PremiumReminderService premiumReminderService;

    @PostMapping("/send-upcoming-reminders")
    public String sendUpcomingReminders() {
        premiumReminderService.sendUpcomingPaymentReminders();
        return "Upcoming payment reminders sent successfully!";
    }

    @PostMapping("/mark-overdue")
    public String markOverduePayments() {
        premiumReminderService.markOverduePayments();
        return "Overdue payments marked successfully!";
    }

    @PostMapping("/run-full-check")
    public String runFullCheck() {
        premiumReminderService.checkAndSendReminders();
        return "Full reminder check completed successfully!";
    }
}
