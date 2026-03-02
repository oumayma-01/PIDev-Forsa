package org.example.forsapidev.DTO;

public class InsuranceCompleteQuoteDTO {
    private PremiumCalculationResultDTO premiumDetails;
    private InsuranceAmortizationScheduleDTO amortizationSchedule;

    // Constructor
    public InsuranceCompleteQuoteDTO() {}

    // Getters and Setters
    public PremiumCalculationResultDTO getPremiumDetails() { return premiumDetails; }
    public void setPremiumDetails(PremiumCalculationResultDTO premiumDetails) {
        this.premiumDetails = premiumDetails;
    }

    public InsuranceAmortizationScheduleDTO getAmortizationSchedule() { return amortizationSchedule; }
    public void setAmortizationSchedule(InsuranceAmortizationScheduleDTO amortizationSchedule) {
        this.amortizationSchedule = amortizationSchedule;
    }
}