package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.Services.Interfaces.IInsuranceProduct;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/insurance-product")
public class InsuranceProductController {

    IInsuranceProduct insuranceProductService;

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-all-insurance-products")
    public List<InsuranceProduct> retrieveAllInsuranceProducts() {
        List<InsuranceProduct> products = insuranceProductService.retrieveAllInsuranceProducts();
        return products;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add-insurance-product")
    public InsuranceProduct addInsuranceProduct(@RequestBody InsuranceProduct product) {
        InsuranceProduct insuranceProduct = insuranceProductService.addInsuranceProduct(product);
        return insuranceProduct;
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-insurance-product/{id}")
    public InsuranceProduct retrieveInsuranceProduct(@PathVariable("product-id") Long productId) {
        InsuranceProduct product = insuranceProductService.retrieveInsuranceProduct(productId);
        return product;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/remove-insurance-product/{product-id}")
    public void removeInsuranceProduct(@PathVariable("product-id") Long productId) {
        insuranceProductService.removeInsuranceProduct(productId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/modify-insurance-product")    public InsuranceProduct modifyInsuranceProduct(@RequestBody InsuranceProduct product) {
        InsuranceProduct insuranceProduct = insuranceProductService.modifyInsuranceProduct(product);
        return insuranceProduct;
    }

    @PutMapping("/affect-policies/{product-id}")
    public InsuranceProduct affectPoliciesToProduct(@RequestBody List<Long> policyIds, @PathVariable("product-id") Long productId) {
        return insuranceProductService.affectPoliciesToProduct(policyIds, productId);
    }
}