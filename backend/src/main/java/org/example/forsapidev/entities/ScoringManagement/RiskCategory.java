package org.example.forsapidev.entities.ScoringManagement;

public enum RiskCategory {
    EXCELLENT,      // 80-100
    GOOD,          // 65-79
    MODERATE,      // 50-64
    RISKY,         // 35-49
    VERY_RISKY;    // 0-34

    public static RiskCategory fromScore(double score) {
        if (score >= 80) return EXCELLENT;
        if (score >= 65) return GOOD;
        if (score >= 50) return MODERATE;
        if (score >= 35) return RISKY;
        return VERY_RISKY;
    }
}