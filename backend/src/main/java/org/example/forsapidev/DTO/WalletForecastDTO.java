package org.example.forsapidev.DTO;

import java.math.BigDecimal;

public class WalletForecastDTO {

    private BigDecimal currentBalance;
    private BigDecimal predictedBalance;
    private int        forecastDays;
    private String     trend;
    private String     explanation;

    public WalletForecastDTO() {}

    public WalletForecastDTO(BigDecimal currentBalance,
                             BigDecimal predictedBalance,
                             int forecastDays,
                             String trend,
                             String explanation) {
        this.currentBalance   = currentBalance;
        this.predictedBalance = predictedBalance;
        this.forecastDays     = forecastDays;
        this.trend            = trend;
        this.explanation      = explanation;
    }

    public BigDecimal getCurrentBalance()            { return currentBalance; }
    public void setCurrentBalance(BigDecimal v)      { this.currentBalance = v; }
    public BigDecimal getPredictedBalance()          { return predictedBalance; }
    public void setPredictedBalance(BigDecimal v)    { this.predictedBalance = v; }
    public int getForecastDays()                     { return forecastDays; }
    public void setForecastDays(int v)               { this.forecastDays = v; }
    public String getTrend()                         { return trend; }
    public void setTrend(String v)                   { this.trend = v; }
    public String getExplanation()                   { return explanation; }
    public void setExplanation(String v)             { this.explanation = v; }
}