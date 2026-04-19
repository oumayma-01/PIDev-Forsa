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

    @Override
    public List<InsuranceClaim> retrieveAllInsuranceClaims() {
        return insuranceClaimRepository.findAll();
    }

    @Override
    public List<InsuranceClaim> retrieveMyClaims(Long userId) {
        return insuranceClaimRepository.findAll().stream()
                .filter(c -> c.getInsurancePolicy() != null 
                        && c.getInsurancePolicy().getUser() != null 
                        && c.getInsurancePolicy().getUser().getId().equals(userId))
                .toList();
    }

    @Override
    public InsuranceClaim retrieveInsuranceClaim(Long claimId) {
        return insuranceClaimRepository.findById(claimId).get();
    }

    @Override
    public InsuranceClaim addInsuranceClaim(InsuranceClaim claim) {
        return insuranceClaimRepository.save(claim);
    }

    @Override
    public void removeInsuranceClaim(Long claimId) {
        insuranceClaimRepository.deleteById(claimId);
    }

    @Override
    public InsuranceClaim modifyInsuranceClaim(InsuranceClaim claim) {
        InsuranceClaim existing = insuranceClaimRepository.findById(claim.getId()).get();
        existing.setClaimNumber(claim.getClaimNumber());
        existing.setClaimDate(claim.getClaimDate());
        existing.setIncidentDate(claim.getIncidentDate());
        existing.setClaimAmount(claim.getClaimAmount());
        existing.setApprovedAmount(claim.getApprovedAmount());
        existing.setDescription(claim.getDescription());
        existing.setStatus(claim.getStatus());
        existing.setIndemnificationPaid(claim.getIndemnificationPaid());
        return insuranceClaimRepository.save(existing);
    }
}