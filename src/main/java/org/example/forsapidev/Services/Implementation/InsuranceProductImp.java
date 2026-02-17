package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Services.Interfaces.IInsuranceProduct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class InsuranceProductImp implements IInsuranceProduct {
    InsuranceProductRepository insuranceProductRepository;

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
}