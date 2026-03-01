package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Config.ActuarialConstants;
import org.example.forsapidev.DTO.InsuranceAmortizationLineDTO;
import org.example.forsapidev.DTO.InsuranceAmortizationScheduleDTO;
import org.example.forsapidev.Services.Interfaces.IInsuranceAmortizationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class InsuranceAmortizationService implements IInsuranceAmortizationService {

    @Override
    public InsuranceAmortizationScheduleDTO generateAmortizationSchedule(
            BigDecimal principal,
            Double annualRate,
            Integer durationMonths,
            String paymentFrequency,
            Date startDate) {

        int periodsPerYear = getPeriodsPerYear(paymentFrequency);
        double periodicRate = annualRate / periodsPerYear;
        int totalPeriods = (durationMonths / 12) * periodsPerYear;

        // Calculate fixed payment using formula: M = P × [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal fixedPayment = calculateFixedPayment(principal, periodicRate, totalPeriods);

        // Generate schedule
        List<InsuranceAmortizationLineDTO> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        Date currentDate = startDate;

        for (int period = 1; period <= totalPeriods; period++) {
            // Calculate interest portion: I = Balance × r
            BigDecimal interestPortion = remainingBalance.multiply(BigDecimal.valueOf(periodicRate))
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate principal portion: P = Payment - Interest
            BigDecimal principalPortion = fixedPayment.subtract(interestPortion)
                    .setScale(2, RoundingMode.HALF_UP);

            // Update remaining balance
            remainingBalance = remainingBalance.subtract(principalPortion)
                    .setScale(2, RoundingMode.HALF_UP);

            // Handle final period rounding
            if (period == totalPeriods && remainingBalance.compareTo(BigDecimal.ZERO) != 0) {
                principalPortion = principalPortion.add(remainingBalance);
                remainingBalance = BigDecimal.ZERO;
            }

            // Calculate due date
            Date dueDate = calculateDueDate(currentDate, period, paymentFrequency);

            // Add line to schedule
            schedule.add(new InsuranceAmortizationLineDTO(
                    period,
                    dueDate,
                    fixedPayment,
                    interestPortion,
                    principalPortion,
                    remainingBalance
            ));

            totalInterest = totalInterest.add(interestPortion);
        }

        // Build result
        InsuranceAmortizationScheduleDTO result = new InsuranceAmortizationScheduleDTO();
        result.setTotalPrincipal(principal);
        result.setPeriodicPayment(fixedPayment);
        result.setAnnualInterestRate(annualRate);
        result.setPaymentFrequency(paymentFrequency);
        result.setNumberOfPayments(totalPeriods);
        result.setTotalInterest(totalInterest);
        result.setTotalAmountPaid(principal.add(totalInterest));
        result.setSchedule(schedule);

        System.out.println("✅ Amortization schedule generated: " + totalPeriods + " payments");

        return result;
    }

    /**
     * Formula: M = P × [r(1+r)^n] / [(1+r)^n - 1]
     */
    private BigDecimal calculateFixedPayment(BigDecimal principal, double periodicRate, int totalPeriods) {
        if (periodicRate == 0) {
            // No interest case
            return principal.divide(BigDecimal.valueOf(totalPeriods), 2, RoundingMode.HALF_UP);
        }

        double factor = Math.pow(1 + periodicRate, totalPeriods);
        double numerator = periodicRate * factor;
        double denominator = factor - 1;

        return principal.multiply(BigDecimal.valueOf(numerator / denominator))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int getPeriodsPerYear(String paymentFrequency) {
        switch (paymentFrequency.toUpperCase()) {
            case "MONTHLY":
                return ActuarialConstants.MONTHLY_PERIODS;
            case "QUARTERLY":
                return ActuarialConstants.QUARTERLY_PERIODS;
            case "SEMI_ANNUAL":
                return ActuarialConstants.SEMI_ANNUAL_PERIODS;
            case "ANNUAL":
                return ActuarialConstants.ANNUAL_PERIODS;
            default:
                return ActuarialConstants.MONTHLY_PERIODS;
        }
    }

    private Date calculateDueDate(Date startDate, int period, String paymentFrequency) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        switch (paymentFrequency.toUpperCase()) {
            case "MONTHLY":
                calendar.add(Calendar.MONTH, period);
                break;
            case "QUARTERLY":
                calendar.add(Calendar.MONTH, period * 3);
                break;
            case "SEMI_ANNUAL":
                calendar.add(Calendar.MONTH, period * 6);
                break;
            case "ANNUAL":
                calendar.add(Calendar.YEAR, period);
                break;
            default:
                calendar.add(Calendar.MONTH, period);
        }

        return calendar.getTime();
    }
}