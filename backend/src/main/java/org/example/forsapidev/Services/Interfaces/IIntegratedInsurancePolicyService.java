package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.InsurancePolicyApplicationDTO;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;

public interface IIntegratedInsurancePolicyService {
    InsurancePolicy createPolicyWithActuarialCalculations(InsurancePolicyApplicationDTO application) throws Exception;
}