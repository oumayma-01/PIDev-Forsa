package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import java.util.List;

public interface IInsuranceProduct {
    public List<InsuranceProduct> retrieveAllInsuranceProducts();
    public InsuranceProduct retrieveInsuranceProduct(Long productId);
    public InsuranceProduct addInsuranceProduct(InsuranceProduct product);
    public void removeInsuranceProduct(Long productId);
    public InsuranceProduct modifyInsuranceProduct(InsuranceProduct product);
    public InsuranceProduct affectPoliciesToProduct(List<Long> policyIds, Long productId);
}