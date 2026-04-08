package org.example.forsapidev.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour recevoir la réponse du service IA de scoring
 */
public class ScoringResponseDto {

    @JsonProperty("score")
    private double score;

    @JsonProperty("risky")
    private boolean risky;

    @JsonProperty("risk_level")
    private String riskLevel;

    // Constructors
    public ScoringResponseDto() {
    }

    public ScoringResponseDto(double score, boolean risky) {
        this.score = score;
        this.risky = risky;
    }

    // Getters and Setters
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isRisky() {
        return risky;
    }

    public void setRisky(boolean risky) {
        this.risky = risky;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}

