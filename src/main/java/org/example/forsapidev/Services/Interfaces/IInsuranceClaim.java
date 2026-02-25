package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import java.util.List;

public interface IInsuranceClaim {
    public List<InsuranceClaim> retrieveAllInsuranceClaims();
    public InsuranceClaim retrieveInsuranceClaim(Long claimId);
    public InsuranceClaim addInsuranceClaim(InsuranceClaim claim);
    public void removeInsuranceClaim(Long claimId);
    public InsuranceClaim modifyInsuranceClaim(InsuranceClaim claim);
}