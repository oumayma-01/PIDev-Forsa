package org.example.forsapidev.Controllers;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.Services.Interfaces.IInsuranceProduct;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/insurance-product")
public class InsuranceProductController {

    IInsuranceProduct insuranceProductService;

    @GetMapping("/retrieve-all-insurance-products")
    public List<InsuranceProduct> retrieveAllInsuranceProducts() {
        List<InsuranceProduct> products = insuranceProductService.retrieveAllInsuranceProducts();
        return products;
    }

    @PostMapping("/add-insurance-product")
    public InsuranceProduct addInsuranceProduct(@RequestBody InsuranceProduct product) {
        InsuranceProduct insuranceProduct = insuranceProductService.addInsuranceProduct(product);
        return insuranceProduct;
    }

    @GetMapping("/retrieve-insurance-product/{product-id}")
    public InsuranceProduct retrieveInsuranceProduct(@PathVariable("product-id") Long productId) {
        InsuranceProduct product = insuranceProductService.retrieveInsuranceProduct(productId);
        return product;
    }

    @DeleteMapping("/remove-insurance-product/{product-id}")
    public void removeInsuranceProduct(@PathVariable("product-id") Long productId) {
        insuranceProductService.removeInsuranceProduct(productId);
    }

    @PutMapping("/modify-insurance-product")
    public InsuranceProduct modifyInsuranceProduct(@RequestBody InsuranceProduct product) {
        InsuranceProduct insuranceProduct = insuranceProductService.modifyInsuranceProduct(product);
        return insuranceProduct;
    }
}