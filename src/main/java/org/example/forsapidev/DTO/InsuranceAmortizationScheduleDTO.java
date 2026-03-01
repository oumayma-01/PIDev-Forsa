package org.example.forsapidev.DTO;

import java.math.BigDecimal;
import java.util.List;

public class InsuranceAmortizationScheduleDTO {
    private BigDecimal totalPrincipal;
    private BigDecimal periodicPayment;
    private Double annualInterestRate;
    private String paymentFrequency;
    private Integer numberOfPayments;
    private BigDecimal totalInterest;
    private BigDecimal totalAmountPaid;
    private List<InsuranceAmortizationLineDTO> schedule;

    // Constructor
    public InsuranceAmortizationScheduleDTO() {}

    // Getters and Setters
    public BigDecimal getTotalPrincipal() { return totalPrincipal; }
    public void setTotalPrincipal(BigDecimal totalPrincipal) { this.totalPrincipal = totalPrincipal; }

    public BigDecimal getPeriodicPayment() { return periodicPayment; }
    public void setPeriodicPayment(BigDecimal periodicPayment) { this.periodicPayment = periodicPayment; }

    public Double getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(Double annualInterestRate) { this.annualInterestRate = annualInterestRate; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public Integer getNumberOfPayments() { return numberOfPayments; }
    public void setNumberOfPayments(Integer numberOfPayments) { this.numberOfPayments = numberOfPayments; }

    public BigDecimal getTotalInterest() { return totalInterest; }
    public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }

    public BigDecimal getTotalAmountPaid() { return totalAmountPaid; }
    public void setTotalAmountPaid(BigDecimal totalAmountPaid) { this.totalAmountPaid = totalAmountPaid; }

    public List<InsuranceAmortizationLineDTO> getSchedule() { return schedule; }
    public void setSchedule(List<InsuranceAmortizationLineDTO> schedule) { this.schedule = schedule; }
}