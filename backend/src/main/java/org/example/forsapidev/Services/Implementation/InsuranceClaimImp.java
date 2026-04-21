package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Services.Interfaces.IInsuranceClaim;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        existing.setAccidentType(claim.getAccidentType());
        existing.setDamagedPoints(claim.getDamagedPoints());
        existing.setAttachmentUrl(claim.getAttachmentUrl());
        return insuranceClaimRepository.save(existing);
    }

    private final String uploadDir = "uploads/claims";

    @Override
    public String uploadAttachment(org.springframework.web.multipart.MultipartFile file) throws IOException {
        Path root = Path.of(System.getProperty("user.dir")).resolve(uploadDir).normalize();
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = root.resolve(fileName);
        file.transferTo(target.toFile());
        return fileName;
    }

    @Override
    public org.springframework.core.io.Resource getAttachment(String fileName) {
        try {
            Path file = Path.of(System.getProperty("user.dir")).resolve(uploadDir).resolve(fileName).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + fileName);
            }
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}