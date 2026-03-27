package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.InsurancePolicyApplicationDTO;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;

import java.math.BigDecimal;
import java.util.List;

public interface IInsurancePolicy {
    public List<InsurancePolicy> retrieveAllInsurancePolicies();
    public InsurancePolicy retrieveInsurancePolicy(Long policyId);
    public InsurancePolicy addInsurancePolicy(InsurancePolicy policy);
    public void removeInsurancePolicy(Long policyId);
    public InsurancePolicy modifyInsurancePolicy(InsurancePolicy policy);
    public InsurancePolicy affectClaimsToPolicy(List<Long> claimIds, Long policyId);

    // NEW METHODS - with throws Exception
    InsurancePolicy clientSubmitApplication(InsurancePolicyApplicationDTO application, Long userId) throws Exception;
    InsurancePolicy agentUpdatePolicy(Long policyId, PolicyStatus status,
                                      BigDecimal approvedCoverage, String notes) throws Exception;

}