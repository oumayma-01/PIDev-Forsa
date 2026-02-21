package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import java.util.List;

public interface IInsurancePolicy {
    public List<InsurancePolicy> retrieveAllInsurancePolicies();
    public InsurancePolicy retrieveInsurancePolicy(Long policyId);
    public InsurancePolicy addInsurancePolicy(InsurancePolicy policy);
    public void removeInsurancePolicy(Long policyId);
    public InsurancePolicy modifyInsurancePolicy(InsurancePolicy policy);
    public InsurancePolicy affectClaimsToPolicy(List<Long> claimIds, Long policyId);
    public InsurancePolicy affectPremiumPaymentsToPolicy(List<Long> paymentIds, Long policyId);
}