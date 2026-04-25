package org.example.forsapidev.entities.AIScoreManagement;

public enum AIScoreLevel {
    VERY_LOW,    // 0-399
    LOW,         // 400-499
    MEDIUM,      // 500-599
    GOOD,        // 600-699
    VERY_GOOD,   // 700-799
    EXCELLENT,   // 800-899
    PREMIUM;     // 900-1000

    public static AIScoreLevel fromScore(int score) {
        if (score < 400) return VERY_LOW;
        if (score < 500) return LOW;
        if (score < 600) return MEDIUM;
        if (score < 700) return GOOD;
        if (score < 800) return VERY_GOOD;
        if (score < 900) return EXCELLENT;
        return PREMIUM;
    }

    public double getMultiplier() {
        switch (this) {
            case VERY_LOW: return 0.0;
            case LOW: return 1.0;
            case MEDIUM: return 1.5;
            case GOOD: return 2.0;
            case VERY_GOOD: return 2.5;
            case EXCELLENT: return 3.5;
            case PREMIUM: return 4.5;
            default: return 0.0;
        }
    }
}