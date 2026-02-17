package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Services.Interfaces.IInsuranceClaim;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class InsuranceClaimImp implements IInsuranceClaim {
    InsuranceClaimRepository insuranceClaimRepository;

    public List<InsuranceClaim> retrieveAllInsuranceClaims() {
        return insuranceClaimRepository.findAll();
    }

    public InsuranceClaim retrieveInsuranceClaim(Long claimId) {
        return insuranceClaimRepository.findById(claimId).get();
    }

    public InsuranceClaim addInsuranceClaim(InsuranceClaim claim) {
        return insuranceClaimRepository.save(claim);
    }

    public void removeInsuranceClaim(Long claimId) {
        insuranceClaimRepository.deleteById(claimId);
    }

    public InsuranceClaim modifyInsuranceClaim(InsuranceClaim claim) {
        return insuranceClaimRepository.save(claim);
    }
}