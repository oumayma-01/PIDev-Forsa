package org.example.forsapidev.Controllers;

import org.example.forsapidev.DTO.ClaimsDashboardDTO;
import org.example.forsapidev.Services.Interfaces.IClaimsDashboardService;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/claims-dashboard")
@CrossOrigin(origins = "*")  // Allow frontend to access
public class ClaimsDashboardController {

    private final IClaimsDashboardService dashboardService;

    /**
     * Get complete dashboard analytics data
     */
    @GetMapping("/analytics")
    public ClaimsDashboardDTO getDashboardAnalytics() {
        return dashboardService.getDashboardData();
    }
}