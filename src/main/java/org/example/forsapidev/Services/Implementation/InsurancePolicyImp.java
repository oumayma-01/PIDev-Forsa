package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Services.Interfaces.IInsurancePolicy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class InsurancePolicyImp implements IInsurancePolicy {
    InsurancePolicyRepository insurancePolicyRepository;

    public List<InsurancePolicy> retrieveAllInsurancePolicies() {
        return insurancePolicyRepository.findAll();
    }

    public InsurancePolicy retrieveInsurancePolicy(Long policyId) {
        return insurancePolicyRepository.findById(policyId).get();
    }

    public InsurancePolicy addInsurancePolicy(InsurancePolicy policy) {
        return insurancePolicyRepository.save(policy);
    }

    public void removeInsurancePolicy(Long policyId) {
        insurancePolicyRepository.deleteById(policyId);
    }

    public InsurancePolicy modifyInsurancePolicy(InsurancePolicy policy) {
        return insurancePolicyRepository.save(policy);
    }
}