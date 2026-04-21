package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import java.util.List;

public interface IInsuranceClaim {
    public List<InsuranceClaim> retrieveAllInsuranceClaims();
    public InsuranceClaim retrieveInsuranceClaim(Long claimId);
    public InsuranceClaim addInsuranceClaim(InsuranceClaim claim);
    public void removeInsuranceClaim(Long claimId);
    public InsuranceClaim modifyInsuranceClaim(InsuranceClaim claim);
    public List<InsuranceClaim> retrieveMyClaims(Long userId);
    public String uploadAttachment(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
    public org.springframework.core.io.Resource getAttachment(String fileName);
}