package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.DTO.AdminFinancialDashboardDTO;
import org.example.forsapidev.Services.Implementation.AdminFinancialAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/dashboard/admin-financial")
@CrossOrigin(origins = "*")
public class AdminFinancialAnalyticsController {

    private final AdminFinancialAnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminFinancialDashboardDTO getOverview() {
        return analyticsService.getDashboardAnalytics();
    }
}
