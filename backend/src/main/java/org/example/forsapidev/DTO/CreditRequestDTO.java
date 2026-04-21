package org.example.forsapidev.DTO;

import org.example.forsapidev.entities.CreditManagement.AmortizationType;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.CreditManagement.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de sortie pour CreditRequest.
 * Objectif: ne jamais sérialiser l'entité JPA directement (évite boucles infinies et Hibernate proxies).
 */
public class CreditRequestDTO {

    private Long id;

    private BigDecimal amountRequested;
    private Double interestRate;
    private Integer durationMonths;
    private CreditStatus status;
    private LocalDateTime requestDate;

    private Long agentId;
    private UserRefDTO user;

    private AmortizationType typeCalcul;

    private BigDecimal remainingBalance;
    private Integer paidInstallments;

    private Boolean isRisky;
    private RiskLevel riskLevel;
    private LocalDateTime scoredAt;

    private String healthReportPath;
    private String originalHealthReportFilename;

    private BigDecimal insuranceRate;
    private BigDecimal insuranceAmount;
    private Boolean insuranceIsReject;
    private String insuranceRating;
    private String insuranceScoringReport;
    private LocalDateTime insurancePaidAt;

    private String globalDecision;
    private String globalPdfPath;
    private String fraudReportPath;

    public CreditRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public CreditStatus getStatus() {
        return status;
    }

    public void setStatus(CreditStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public UserRefDTO getUser() {
        return user;
    }

    public void setUser(UserRefDTO user) {
        this.user = user;
    }

    public AmortizationType getTypeCalcul() {
        return typeCalcul;
    }

    public void setTypeCalcul(AmortizationType typeCalcul) {
        this.typeCalcul = typeCalcul;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Integer getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(Integer paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public Boolean getIsRisky() {
        return isRisky;
    }

    public void setIsRisky(Boolean risky) {
        isRisky = risky;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getScoredAt() {
        return scoredAt;
    }

    public void setScoredAt(LocalDateTime scoredAt) {
        this.scoredAt = scoredAt;
    }

    public String getHealthReportPath() {
        return healthReportPath;
    }

    public void setHealthReportPath(String healthReportPath) {
        this.healthReportPath = healthReportPath;
    }

    public String getOriginalHealthReportFilename() {
        return originalHealthReportFilename;
    }

    public void setOriginalHealthReportFilename(String originalHealthReportFilename) {
        this.originalHealthReportFilename = originalHealthReportFilename;
    }

    public BigDecimal getInsuranceRate() {
        return insuranceRate;
    }

    public void setInsuranceRate(BigDecimal insuranceRate) {
        this.insuranceRate = insuranceRate;
    }

    public BigDecimal getInsuranceAmount() {
        return insuranceAmount;
    }

    public void setInsuranceAmount(BigDecimal insuranceAmount) {
        this.insuranceAmount = insuranceAmount;
    }

    public Boolean getInsuranceIsReject() {
        return insuranceIsReject;
    }

    public void setInsuranceIsReject(Boolean insuranceIsReject) {
        this.insuranceIsReject = insuranceIsReject;
    }

    public String getInsuranceRating() {
        return insuranceRating;
    }

    public void setInsuranceRating(String insuranceRating) {
        this.insuranceRating = insuranceRating;
    }

    public String getInsuranceScoringReport() {
        return insuranceScoringReport;
    }

    public void setInsuranceScoringReport(String insuranceScoringReport) {
        this.insuranceScoringReport = insuranceScoringReport;
    }

    public LocalDateTime getInsurancePaidAt() {
        return insurancePaidAt;
    }

    public void setInsurancePaidAt(LocalDateTime insurancePaidAt) {
        this.insurancePaidAt = insurancePaidAt;
    }

    public String getGlobalDecision() {
        return globalDecision;
    }

    public void setGlobalDecision(String globalDecision) {
        this.globalDecision = globalDecision;
    }

    public String getGlobalPdfPath() {
        return globalPdfPath;
    }

    public void setGlobalPdfPath(String globalPdfPath) {
        this.globalPdfPath = globalPdfPath;
    }

    public String getFraudReportPath() {
        return fraudReportPath;
    }

    public void setFraudReportPath(String fraudReportPath) {
        this.fraudReportPath = fraudReportPath;
    }
}
