package org.example.forsapidev.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO pour recevoir la réponse de l'API Python unifiée (port 8000)
 * Contient à la fois le scoring de fraude ET le taux d'assurance
 */
public class UnifiedCreditAnalysisResponseDto {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("generated_at")
    private String generatedAt;

    // Fraud Analysis
    @JsonProperty("fraud_risk_probability")
    private Double fraudRiskProbability;

    @JsonProperty("fraud_risk_category")
    private String fraudRiskCategory;

    @JsonProperty("fraud_risk_factors")
    private List<String> fraudRiskFactors;

    @JsonProperty("fraud_report_path")
    private String fraudReportPath;

    // Insurance Analysis
    @JsonProperty("insurance_ai_result")
    private String insuranceAiResult;

    @JsonProperty("insurance_rating")
    private String insuranceRating;

    @JsonProperty("insurance_rate")
    private Double insuranceRate;

    @JsonProperty("insurance_is_reject")
    private Boolean insuranceIsReject;

    @JsonProperty("insurance_scoring_report")
    private String insuranceScoringReport;

    // Global Results
    @JsonProperty("global_report_pdf_path")
    private String globalReportPdfPath;

    @JsonProperty("global_report_saved")
    private Boolean globalReportSaved;

    @JsonProperty("global_decision")
    private String globalDecision;

    // Constructors
    public UnifiedCreditAnalysisResponseDto() {
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Double getFraudRiskProbability() {
        return fraudRiskProbability;
    }

    public void setFraudRiskProbability(Double fraudRiskProbability) {
        this.fraudRiskProbability = fraudRiskProbability;
    }

    public String getFraudRiskCategory() {
        return fraudRiskCategory;
    }

    public void setFraudRiskCategory(String fraudRiskCategory) {
        this.fraudRiskCategory = fraudRiskCategory;
    }

    public List<String> getFraudRiskFactors() {
        return fraudRiskFactors;
    }

    public void setFraudRiskFactors(List<String> fraudRiskFactors) {
        this.fraudRiskFactors = fraudRiskFactors;
    }

    public String getFraudReportPath() {
        return fraudReportPath;
    }

    public void setFraudReportPath(String fraudReportPath) {
        this.fraudReportPath = fraudReportPath;
    }

    public String getInsuranceAiResult() {
        return insuranceAiResult;
    }

    public void setInsuranceAiResult(String insuranceAiResult) {
        this.insuranceAiResult = insuranceAiResult;
    }

    public String getInsuranceRating() {
        return insuranceRating;
    }

    public void setInsuranceRating(String insuranceRating) {
        this.insuranceRating = insuranceRating;
    }

    public Double getInsuranceRate() {
        return insuranceRate;
    }

    public void setInsuranceRate(Double insuranceRate) {
        this.insuranceRate = insuranceRate;
    }

    public Boolean getInsuranceIsReject() {
        return insuranceIsReject;
    }

    public void setInsuranceIsReject(Boolean insuranceIsReject) {
        this.insuranceIsReject = insuranceIsReject;
    }

    public String getInsuranceScoringReport() {
        return insuranceScoringReport;
    }

    public void setInsuranceScoringReport(String insuranceScoringReport) {
        this.insuranceScoringReport = insuranceScoringReport;
    }

    public String getGlobalReportPdfPath() {
        return globalReportPdfPath;
    }

    public void setGlobalReportPdfPath(String globalReportPdfPath) {
        this.globalReportPdfPath = globalReportPdfPath;
    }

    public Boolean getGlobalReportSaved() {
        return globalReportSaved;
    }

    public void setGlobalReportSaved(Boolean globalReportSaved) {
        this.globalReportSaved = globalReportSaved;
    }

    public String getGlobalDecision() {
        return globalDecision;
    }

    public void setGlobalDecision(String globalDecision) {
        this.globalDecision = globalDecision;
    }
}

