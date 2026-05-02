package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Implementation.AdminDemoDataSeedService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/dashboard/admin-seed")
@CrossOrigin(origins = "*")
public class AdminDemoDataController {

    private final AdminDemoDataSeedService seedService;

    @PostMapping("/financial-insurance")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> seedFinancialInsuranceData(
            @RequestParam(defaultValue = "12") int monthsBack,
            @RequestParam(defaultValue = "18") int recordsPerMonth
    ) {
        return seedService.generateDemoData(monthsBack, recordsPerMonth);
    }
}
