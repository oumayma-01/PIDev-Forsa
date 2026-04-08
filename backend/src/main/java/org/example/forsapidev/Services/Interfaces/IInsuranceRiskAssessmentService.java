package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.InsuranceRiskAssessmentDTO;

public interface IInsuranceRiskAssessmentService {
    InsuranceRiskAssessmentDTO calculateRiskScore(InsuranceRiskAssessmentDTO riskProfile);
}