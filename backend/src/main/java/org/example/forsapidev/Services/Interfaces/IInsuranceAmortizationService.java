package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.InsuranceAmortizationScheduleDTO;
import java.math.BigDecimal;
import java.util.Date;

public interface IInsuranceAmortizationService {
    InsuranceAmortizationScheduleDTO generateAmortizationSchedule(
            BigDecimal principal,
            Double annualRate,
            Integer durationMonths,
            String paymentFrequency,
            Date startDate
    );
}