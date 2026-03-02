package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IPartnerAnalyticsService;
import org.example.forsapidev.entities.PartnershipManagement.PartnerAnalytics;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partner-analytics")
@RequiredArgsConstructor
public class PartnerAnalyticsController {

    private final IPartnerAnalyticsService analyticsService;

    @GetMapping("/{partnerId}/today")
    public ResponseEntity<PartnerAnalytics> getTodayAnalytics(@PathVariable Long partnerId) {
        PartnerAnalytics analytics = analyticsService.getTodayAnalytics(partnerId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{partnerId}/period")
    public ResponseEntity<List<PartnerAnalytics>> getAnalyticsByPeriod(
            @PathVariable Long partnerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<PartnerAnalytics> analytics = analyticsService.getAnalyticsByPeriod(partnerId, start, end);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{partnerId}/dashboard")
    public ResponseEntity<Map<String, Object>> getPartnerDashboard(@PathVariable Long partnerId) {
        Map<String, Object> dashboard = analyticsService.getPartnerDashboard(partnerId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{partnerId}/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStats(@PathVariable Long partnerId) {
        Map<String, Object> stats = analyticsService.getRealtimeStats(partnerId);
        return ResponseEntity.ok(stats);
    }
}