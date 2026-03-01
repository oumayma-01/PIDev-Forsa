package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IScoringFactorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoringFactorService implements IScoringFactorService {

    @Override
    public double calculateRevenueStabilityFactor(Long clientId) {
        log.debug("Calculating revenue stability for client: {}", clientId);

        List<Double> revenues = List.of(1200.0, 1300.0, 1100.0, 1250.0, 1350.0, 1280.0);

        if (revenues.isEmpty()) {
            log.warn("No revenue data for client: {}", clientId);
            return 50.0;
        }

        double mean = revenues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = revenues.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .sum() / revenues.size();
        double stdDev = Math.sqrt(variance);
        double cv = (stdDev / mean) * 100;

        double score;
        if (cv < 10) {
            score = 100;
        } else if (cv < 20) {
            score = 90 - ((cv - 10) * 3);
        } else if (cv < 30) {
            score = 60 - ((cv - 20) * 3);
        } else if (cv < 40) {
            score = 30 - ((cv - 30) * 3);
        } else {
            score = 0;
        }

        log.debug("Revenue stability - Mean: {}, StdDev: {}, CV: {}%, Score: {}",
                mean, stdDev, cv, score);

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public double calculatePaymentHistoryFactor(Long clientId) {
        log.debug("Calculating payment history for client: {}", clientId);

        int totalBills = 12;
        int onTimeBills = 10;
        int minorDelayBills = 2;
        int majorDelayBills = 0;

        if (totalBills == 0) {
            log.warn("No bill payment history for client: {}", clientId);
            return 50.0;
        }

        double score = ((onTimeBills * 1.0) +
                (minorDelayBills * 0.5) +
                (majorDelayBills * 0.0)) / totalBills * 100;

        log.debug("Payment history - Total: {}, OnTime: {}, MinorDelay: {}, Score: {}",
                totalBills, onTimeBills, minorDelayBills, score);

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public double calculateDebtRatioFactor(Long clientId) {
        log.debug("Calculating debt ratio for client: {}", clientId);

        double monthlyIncome = 1500.0;
        double monthlyDebtPayments = 300.0;

        if (monthlyIncome == 0) {
            log.warn("No income data for client: {}", clientId);
            return 0.0;
        }

        double dti = (monthlyDebtPayments / monthlyIncome) * 100;

        double score;
        if (dti < 20) {
            score = 100;
        } else if (dti < 30) {
            score = 90 - ((dti - 20) * 4);
        } else if (dti < 40) {
            score = 50 - ((dti - 30) * 3);
        } else if (dti < 50) {
            score = 20 - ((dti - 40) * 2);
        } else {
            score = 0;
        }

        log.debug("Debt ratio - Income: {}, Debt: {}, DTI: {}%, Score: {}",
                monthlyIncome, monthlyDebtPayments, dti, score);

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public double calculateEmploymentTypeFactor(Long clientId) {
        log.debug("Calculating employment type for client: {}", clientId);

        String employmentType = "SALARIE_CDD";

        double score = switch (employmentType) {
            case "FONCTIONNAIRE_CDI" -> 100.0;
            case "SALARIE_PRIVE_CDI" -> 85.0;
            case "SALARIE_CDD" -> 65.0;
            case "INDEPENDANT_FORMEL" -> 55.0;
            case "INFORMEL_STABLE" -> 40.0;
            case "OCCASIONNEL" -> 25.0;
            case "SANS_EMPLOI" -> 0.0;
            default -> 50.0;
        };

        log.debug("Employment type: {}, Score: {}", employmentType, score);

        return score;
    }

    @Override
    public double calculateRegionFactor(Long clientId) {
        log.debug("Calculating region factor for client: {}", clientId);

        String region = "Tunis";

        double unemploymentRate = switch (region) {
            case "Tunis" -> 8.5;
            case "Ariana" -> 9.2;
            case "Ben Arous" -> 10.1;
            case "Sfax" -> 11.3;
            case "Sousse" -> 12.0;
            case "Nabeul" -> 13.5;
            case "Bizerte" -> 14.2;
            case "Kairouan" -> 20.3;
            case "Kasserine" -> 24.8;
            case "Sidi Bouzid" -> 22.1;
            case "Gafsa" -> 23.5;
            case "Tataouine" -> 18.7;
            default -> 15.0;
        };

        double score;
        if (unemploymentRate < 10) {
            score = 100;
        } else if (unemploymentRate < 15) {
            score = 90 - ((unemploymentRate - 10) * 4);
        } else if (unemploymentRate < 20) {
            score = 70 - ((unemploymentRate - 15) * 3);
        } else {
            score = 55 - ((unemploymentRate - 20) * 2);
        }

        log.debug("Region: {}, Unemployment: {}%, Score: {}",
                region, unemploymentRate, score);

        return Math.max(50, Math.min(100, score));
    }

    @Override
    public double getMonthlyIncome(Long clientId) {
        return 1500.0;
    }

    @Override
    public double getCurrentDTI(Long clientId) {
        return 0.20;
    }
}