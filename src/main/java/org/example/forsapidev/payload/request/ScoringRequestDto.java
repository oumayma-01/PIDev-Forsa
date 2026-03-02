package org.example.forsapidev.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour envoyer les features au service IA de scoring
 */
public class ScoringRequestDto {

    @JsonProperty("avg_delay_days")
    private double avgDelayDays;

    @JsonProperty("payment_instability")
    private double paymentInstability;


    @JsonProperty("monthly_transaction_count")
    private int monthlyTransactionCount;

    @JsonProperty("transaction_amount_std")
    private double transactionAmountStd;

    @JsonProperty("high_risk_country_transaction")
    private int highRiskCountryTransaction;

    @JsonProperty("unusual_night_transaction")
    private int unusualNightTransaction;

    @JsonProperty("address_changed")
    private int addressChanged;

    @JsonProperty("phone_changed")
    private int phoneChanged;

    @JsonProperty("email_changed")
    private int emailChanged;

    @JsonProperty("country_changed")
    private int countryChanged;

    @JsonProperty("income_change_percentage")
    private double incomeChangePercentage;

    @JsonProperty("employment_changed")
    private int employmentChanged;

    @JsonProperty("recent_credit_requests")
    private int recentCreditRequests;

    // Constructors
    public ScoringRequestDto() {
    }

    // Getters and Setters
    public double getAvgDelayDays() {
        return avgDelayDays;
    }

    public void setAvgDelayDays(double avgDelayDays) {
        this.avgDelayDays = avgDelayDays;
    }

    public double getPaymentInstability() {
        return paymentInstability;
    }

    public void setPaymentInstability(double paymentInstability) {
        this.paymentInstability = paymentInstability;
    }


    public int getMonthlyTransactionCount() {
        return monthlyTransactionCount;
    }

    public void setMonthlyTransactionCount(int monthlyTransactionCount) {
        this.monthlyTransactionCount = monthlyTransactionCount;
    }

    public double getTransactionAmountStd() {
        return transactionAmountStd;
    }

    public void setTransactionAmountStd(double transactionAmountStd) {
        this.transactionAmountStd = transactionAmountStd;
    }

    public int getHighRiskCountryTransaction() {
        return highRiskCountryTransaction;
    }

    public void setHighRiskCountryTransaction(int highRiskCountryTransaction) {
        this.highRiskCountryTransaction = highRiskCountryTransaction;
    }

    public int getUnusualNightTransaction() {
        return unusualNightTransaction;
    }

    public void setUnusualNightTransaction(int unusualNightTransaction) {
        this.unusualNightTransaction = unusualNightTransaction;
    }

    public int getAddressChanged() {
        return addressChanged;
    }

    public void setAddressChanged(int addressChanged) {
        this.addressChanged = addressChanged;
    }

    public int getPhoneChanged() {
        return phoneChanged;
    }

    public void setPhoneChanged(int phoneChanged) {
        this.phoneChanged = phoneChanged;
    }

    public int getEmailChanged() {
        return emailChanged;
    }

    public void setEmailChanged(int emailChanged) {
        this.emailChanged = emailChanged;
    }

    public int getCountryChanged() {
        return countryChanged;
    }

    public void setCountryChanged(int countryChanged) {
        this.countryChanged = countryChanged;
    }

    public double getIncomeChangePercentage() {
        return incomeChangePercentage;
    }

    public void setIncomeChangePercentage(double incomeChangePercentage) {
        this.incomeChangePercentage = incomeChangePercentage;
    }

    public int getEmploymentChanged() {
        return employmentChanged;
    }

    public void setEmploymentChanged(int employmentChanged) {
        this.employmentChanged = employmentChanged;
    }

    public int getRecentCreditRequests() {
        return recentCreditRequests;
    }

    public void setRecentCreditRequests(int recentCreditRequests) {
        this.recentCreditRequests = recentCreditRequests;
    }
}



