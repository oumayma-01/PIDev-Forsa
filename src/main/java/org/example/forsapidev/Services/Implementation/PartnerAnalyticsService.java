package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IPartnerAnalyticsService;
import org.example.forsapidev.entities.PartnershipManagement.PartnerAnalytics;
import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerAnalyticsRepository;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerAnalyticsService implements IPartnerAnalyticsService {

    private final PartnerAnalyticsRepository analyticsRepository;
    private final PartnerTransactionRepository transactionRepository;

    @Override
    @Transactional
    public void recordDailyAnalytics(Long partnerId, LocalDate date) {
        log.info("Recording analytics for partner: {} date: {}", partnerId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<PartnerTransaction> transactions = transactionRepository
                .findByPartnerAndDateRange(partnerId, startOfDay, endOfDay);

        if (transactions.isEmpty()) {
            log.debug("No transactions for partner {} on {}", partnerId, date);
            return;
        }

        Integer transactionsCount = transactions.size();
        Double totalVolume = transactions.stream()
                .mapToDouble(PartnerTransaction::getAmount)
                .sum();
        Double commissionEarned = transactions.stream()
                .mapToDouble(PartnerTransaction::getCommissionAmount)
                .sum();
        Double avgTransactionAmount = totalVolume / transactionsCount;

        Integer uniqueCustomers = (int) transactions.stream()
                .map(PartnerTransaction::getClientId)
                .distinct()
                .count();

        Integer peakHour = getPeakHour(partnerId, date);

        PartnerAnalytics analytics = PartnerAnalytics.builder()
                .partnerId(partnerId)
                .date(date)
                .transactionsCount(transactionsCount)
                .totalVolume(totalVolume)
                .commissionEarned(commissionEarned)
                .averageTransactionAmount(avgTransactionAmount)
                .uniqueCustomers(uniqueCustomers)
                .peakHour(peakHour)
                .build();

        analyticsRepository.save(analytics);
    }

    @Override
    public PartnerAnalytics getTodayAnalytics(Long partnerId) {
        LocalDate today = LocalDate.now();
        return analyticsRepository.findByPartnerIdAndDate(partnerId, today)
                .orElseGet(() -> {
                    recordDailyAnalytics(partnerId, today);
                    return analyticsRepository.findByPartnerIdAndDate(partnerId, today)
                            .orElse(null);
                });
    }

    @Override
    public List<PartnerAnalytics> getAnalyticsByPeriod(Long partnerId, LocalDate start, LocalDate end) {
        return analyticsRepository.findByPartnerIdAndDateBetweenOrderByDateAsc(partnerId, start, end);
    }

    @Override
    public Map<String, Object> getPartnerDashboard(Long partnerId) {
        Map<String, Object> dashboard = new HashMap<>();

        PartnerAnalytics today = getTodayAnalytics(partnerId);

        if (today != null) {
            dashboard.put("today", Map.of(
                    "transactions", today.getTransactionsCount(),
                    "volume", today.getTotalVolume(),
                    "commission", today.getCommissionEarned(),
                    "avgAmount", today.getAverageTransactionAmount()
            ));
        }

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now();

        List<PartnerAnalytics> monthData = getAnalyticsByPeriod(partnerId, monthStart, monthEnd);

        Integer monthTransactions = monthData.stream()
                .mapToInt(PartnerAnalytics::getTransactionsCount)
                .sum();
        Double monthVolume = monthData.stream()
                .mapToDouble(PartnerAnalytics::getTotalVolume)
                .sum();
        Double monthCommission = monthData.stream()
                .mapToDouble(PartnerAnalytics::getCommissionEarned)
                .sum();

        dashboard.put("thisMonth", Map.of(
                "transactions", monthTransactions,
                "volume", monthVolume,
                "commission", monthCommission
        ));

        List<Map<String, Object>> chartData = monthData.stream()
                .map(a -> Map.<String, Object>of(
                        "date", a.getDate().toString(),
                        "transactions", a.getTransactionsCount(),
                        "volume", a.getTotalVolume()
                ))
                .collect(Collectors.toList());

        dashboard.put("chartData", chartData);

        return dashboard;
    }

    @Override
    public Map<String, Object> getRealtimeStats(Long partnerId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        List<PartnerTransaction> todayTransactions = transactionRepository
                .findByPartnerAndDateRange(partnerId, todayStart, now);

        stats.put("todayTransactionsCount", todayTransactions.size());
        stats.put("todayVolume", todayTransactions.stream()
                .mapToDouble(PartnerTransaction::getAmount).sum());

        LocalDateTime lastHourStart = now.minusHours(1);
        List<PartnerTransaction> lastHourTransactions = transactionRepository
                .findByPartnerAndDateRange(partnerId, lastHourStart, now);

        stats.put("lastHourTransactions", lastHourTransactions.size());

        return stats;
    }

    @Override
    public Integer getPeakHour(Long partnerId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<PartnerTransaction> transactions = transactionRepository
                .findByPartnerAndDateRange(partnerId, startOfDay, endOfDay);

        if (transactions.isEmpty()) {
            return null;
        }

        Map<Integer, Long> hourCounts = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void generateDailyAnalyticsForAllPartners() {
        log.info("Generating daily analytics for all partners");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Long> partnerIds = transactionRepository.findAll().stream()
                .map(PartnerTransaction::getPartnerId)
                .distinct()
                .collect(Collectors.toList());

        partnerIds.forEach(partnerId -> {
            try {
                recordDailyAnalytics(partnerId, yesterday);
            } catch (Exception e) {
                log.error("Error generating analytics for partner: {}", partnerId, e);
            }
        });
    }
}