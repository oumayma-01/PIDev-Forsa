package org.example.forsapidev.Services.Interfaces;

public interface IScoringFactorService {
    double calculateRevenueStabilityFactor(Long clientId);
    double calculatePaymentHistoryFactor(Long clientId);
    double calculateDebtRatioFactor(Long clientId);
    double calculateEmploymentTypeFactor(Long clientId);
    double calculateRegionFactor(Long clientId);
    double getMonthlyIncome(Long clientId);
    double getCurrentDTI(Long clientId);
}