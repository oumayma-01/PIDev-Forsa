package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ScoringManagement.RiskMetrics;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;

public interface IRiskCalculationService {
    RiskMetrics calculateRiskMetrics(ScoreResult scoreResult, Double loanAmount, Integer durationMonths);
    RiskMetrics getLatestRiskMetrics(Long clientId);
}