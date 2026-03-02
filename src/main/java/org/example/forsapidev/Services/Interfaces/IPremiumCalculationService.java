package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.PremiumCalculationRequestDTO;
import org.example.forsapidev.DTO.PremiumCalculationResultDTO;

public interface IPremiumCalculationService {
    PremiumCalculationResultDTO calculatePremium(PremiumCalculationRequestDTO request);
}