package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Services.Interfaces.IInsuranceProduct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class InsuranceProductImp implements IInsuranceProduct {
    InsuranceProductRepository insuranceProductRepository;
    InsurancePolicyRepository insurancePolicyRepository;

    public List<InsuranceProduct> retrieveAllInsuranceProducts() {
        return insuranceProductRepository.findAll();
    }

    public InsuranceProduct retrieveInsuranceProduct(Long productId) {
        return insuranceProductRepository.findById(productId).get();
    }

    public InsuranceProduct addInsuranceProduct(InsuranceProduct product) {
        return insuranceProductRepository.save(product);
    }

    public void removeInsuranceProduct(Long productId) {
        insuranceProductRepository.deleteById(productId);
    }

    public InsuranceProduct modifyInsuranceProduct(InsuranceProduct product) {
        return insuranceProductRepository.save(product);
    }

    @Override
    public InsuranceProduct affectPoliciesToProduct(List<Long> policyIds, Long productId) {
        InsuranceProduct product = insuranceProductRepository.findById(productId).get();
        List<InsurancePolicy> policies = insurancePolicyRepository.findAllById(policyIds);

        for (InsurancePolicy policy : policies) {
            policy.setInsuranceProduct(product);
        }

        product.setPolicies(new HashSet<>(policies));
        return insuranceProductRepository.save(product);
    }
}