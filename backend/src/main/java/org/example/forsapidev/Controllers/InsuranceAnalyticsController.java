package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.example.forsapidev.DTO.InsuranceOverviewDTO;
import org.example.forsapidev.Services.Implementation.InsuranceAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/insurance-analytics")
@CrossOrigin(origins = "*")
public class InsuranceAnalyticsController {

    private final InsuranceAnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public InsuranceOverviewDTO getOverview() {
        System.out.println("📥 Incoming Analytics Request...");
        InsuranceOverviewDTO data = analyticsService.getGlobalOverview();
        System.out.println("📤 Analytics Response Sent");
        return data;
    }
}
