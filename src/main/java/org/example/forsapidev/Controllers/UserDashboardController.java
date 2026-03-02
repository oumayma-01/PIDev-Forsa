package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IUserDashboardService;
import org.example.forsapidev.payload.response.UserDashboardOverviewDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/users")
@RequiredArgsConstructor
public class UserDashboardController {

    private final IUserDashboardService userDashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public UserDashboardOverviewDTO getOverview() {
        return userDashboardService.getOverviewStats();
    }
}