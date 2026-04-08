package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.PartnerAnalytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IPartnerAnalyticsService {
    void recordDailyAnalytics(Long partnerId, LocalDate date);
    PartnerAnalytics getTodayAnalytics(Long partnerId);
    List<PartnerAnalytics> getAnalyticsByPeriod(Long partnerId, LocalDate start, LocalDate end);
    Map<String, Object> getPartnerDashboard(Long partnerId);
    Map<String, Object> getRealtimeStats(Long partnerId);
    Integer getPeakHour(Long partnerId, LocalDate date);
}