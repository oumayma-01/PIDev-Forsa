package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import org.example.forsapidev.entities.UserManagement.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "credit_request")
public class CreditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amountRequested;

    private Double interestRate;

    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    private CreditStatus status;

    private LocalDateTime requestDate;

    private Long agentId;

    /**
     * Client qui a fait la demande de crédit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Type de calcul du crédit (ANNUITE_CONSTANTE ou AMORTISSEMENT_CONSTANT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "amortization_type")
    private AmortizationType typeCalcul = AmortizationType.AMORTISSEMENT_CONSTANT;

    // Fusion tracking fields (no RepaymentSchedule entity)
    @Column(precision = 18, scale = 2)
    private BigDecimal remainingBalance;

    private Integer paidInstallments;

    // AI Scoring fields
    @Column(name = "is_risky")
    private Boolean isRisky;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    // Insurance & Health Report fields
    @Column(name = "health_report_path")
    private String healthReportPath;

    @Column(name = "original_health_report_filename")
    private String originalHealthReportFilename;

    @Column(name = "insurance_rate", precision = 5, scale = 2)
    private BigDecimal insuranceRate;

    @Column(name = "insurance_amount", precision = 18, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "insurance_is_reject")
    private Boolean insuranceIsReject;

    @Column(name = "insurance_rating")
    private String insuranceRating;

    @Column(name = "insurance_scoring_report", columnDefinition = "TEXT")
    private String insuranceScoringReport;


    @Column(name = "insurance_paid_at")
    private LocalDateTime insurancePaidAt;

    @Column(name = "global_decision")
    private String globalDecision;

    @Column(name = "global_pdf_path")
    private String globalPdfPath;

    @Column(name = "fraud_report_path")
    private String fraudReportPath;

    // Constructors
    public CreditRequest() {
    }

    public CreditRequest(Long id, BigDecimal amountRequested, Double interestRate, Integer durationMonths, CreditStatus status, LocalDateTime requestDate, Long agentId) {
        this.id = id;
        this.amountRequested = amountRequested;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.status = status;
        this.requestDate = requestDate;
        this.agentId = agentId;
    }

    // Getters & Setters
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public AmortizationType getTypeCalcul() {
        return typeCalcul;
    }

    public void setTypeCalcul(AmortizationType typeCalcul) {
        this.typeCalcul = typeCalcul;
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

    // Insurance & Health Report getters/setters
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditRequest that = (CreditRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CreditRequest{" +
                "id=" + id +
                ", amountRequested=" + amountRequested +
                ", interestRate=" + interestRate +
                ", durationMonths=" + durationMonths +
                ", status=" + status +
                ", requestDate=" + requestDate +
                ", agentId=" + agentId +
                ", remainingBalance=" + remainingBalance +
                ", paidInstallments=" + paidInstallments +
                '}';
    }
}
