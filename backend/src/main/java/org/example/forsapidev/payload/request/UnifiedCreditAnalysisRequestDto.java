package org.example.forsapidev.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour envoyer les données client à l'API Python unifiée (port 8000)
 */
public class UnifiedCreditAnalysisRequestDto {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("avg_delay_days")
    private Double avgDelayDays;

    @JsonProperty("payment_instability")
    private Double paymentInstability;

    @JsonProperty("credit_utilization")
    private Double creditUtilization;

    @JsonProperty("monthly_transaction_count")
    private Integer monthlyTransactionCount;

    @JsonProperty("transaction_amount_std")
    private Double transactionAmountStd;

    @JsonProperty("high_risk_country_transaction")
    private Integer highRiskCountryTransaction;

    @JsonProperty("unusual_night_transaction")
    private Integer unusualNightTransaction;

    @JsonProperty("address_changed")
    private Integer addressChanged;

    @JsonProperty("phone_changed")
    private Integer phoneChanged;

    @JsonProperty("email_changed")
    private Integer emailChanged;

    @JsonProperty("country_changed")
    private Integer countryChanged;

    @JsonProperty("income_change_percentage")
    private Double incomeChangePercentage;

    @JsonProperty("employment_changed")
    private Integer employmentChanged;

    @JsonProperty("recent_credit_requests")
    private Integer recentCreditRequests;

    // Constructors
    public UnifiedCreditAnalysisRequestDto() {
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Double getAvgDelayDays() {
        return avgDelayDays;
    }

    public void setAvgDelayDays(Double avgDelayDays) {
        this.avgDelayDays = avgDelayDays;
    }

    public Double getPaymentInstability() {
        return paymentInstability;
    }

    public void setPaymentInstability(Double paymentInstability) {
        this.paymentInstability = paymentInstability;
    }

    public Double getCreditUtilization() {
        return creditUtilization;
    }

    public void setCreditUtilization(Double creditUtilization) {
        this.creditUtilization = creditUtilization;
    }

    public Integer getMonthlyTransactionCount() {
        return monthlyTransactionCount;
    }

    public void setMonthlyTransactionCount(Integer monthlyTransactionCount) {
        this.monthlyTransactionCount = monthlyTransactionCount;
    }

    public Double getTransactionAmountStd() {
        return transactionAmountStd;
    }

    public void setTransactionAmountStd(Double transactionAmountStd) {
        this.transactionAmountStd = transactionAmountStd;
    }

    public Integer getHighRiskCountryTransaction() {
        return highRiskCountryTransaction;
    }

    public void setHighRiskCountryTransaction(Integer highRiskCountryTransaction) {
        this.highRiskCountryTransaction = highRiskCountryTransaction;
    }

    public Integer getUnusualNightTransaction() {
        return unusualNightTransaction;
    }

    public void setUnusualNightTransaction(Integer unusualNightTransaction) {
        this.unusualNightTransaction = unusualNightTransaction;
    }

    public Integer getAddressChanged() {
        return addressChanged;
    }

    public void setAddressChanged(Integer addressChanged) {
        this.addressChanged = addressChanged;
    }

    public Integer getPhoneChanged() {
        return phoneChanged;
    }

    public void setPhoneChanged(Integer phoneChanged) {
        this.phoneChanged = phoneChanged;
    }

    public Integer getEmailChanged() {
        return emailChanged;
    }

    public void setEmailChanged(Integer emailChanged) {
        this.emailChanged = emailChanged;
    }

    public Integer getCountryChanged() {
        return countryChanged;
    }

    public void setCountryChanged(Integer countryChanged) {
        this.countryChanged = countryChanged;
    }

    public Double getIncomeChangePercentage() {
        return incomeChangePercentage;
    }

    public void setIncomeChangePercentage(Double incomeChangePercentage) {
        this.incomeChangePercentage = incomeChangePercentage;
    }

    public Integer getEmploymentChanged() {
        return employmentChanged;
    }

    public void setEmploymentChanged(Integer employmentChanged) {
        this.employmentChanged = employmentChanged;
    }

    public Integer getRecentCreditRequests() {
        return recentCreditRequests;
    }

    public void setRecentCreditRequests(Integer recentCreditRequests) {
        this.recentCreditRequests = recentCreditRequests;
    }
}

