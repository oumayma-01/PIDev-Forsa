package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.ComparisonResultDTO;
import org.example.forsapidev.DTO.InsuranceProductComparisonDTO;
import org.example.forsapidev.Services.Interfaces.IComparisonPdfService;
import org.example.forsapidev.Services.Interfaces.IInsuranceProductComparisonService;
import org.example.forsapidev.Services.Interfaces.IInsuranceProduct;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/product-comparison")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductComparisonController {

    private final IInsuranceProductComparisonService comparisonService;
    private final IComparisonPdfService pdfService;
    private final IInsuranceProduct insuranceProductService;  // Added this

    /**
     * Get all insurance products for selection dropdown
     * This is what your HTML calls first
     */
    @GetMapping("/products")
    public ResponseEntity<List<InsuranceProductComparisonDTO>> getAllProducts() {
        try {
            // Get all products from database
            List<InsuranceProduct> products = insuranceProductService.retrieveAllInsuranceProducts();

            // Convert to DTOs with calculated fields
            List<InsuranceProductComparisonDTO> dtos = products.stream()
                    .map(this::convertToComparisonDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Compare 2-3 insurance products
     * Example: /compare?productIds=1,2,3
     */
    @GetMapping("/compare")
    public ResponseEntity<ComparisonResultDTO> compareProducts(@RequestParam List<Long> productIds) {
        try {
            ComparisonResultDTO result = comparisonService.compareProducts(productIds);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Download comparison as PDF
     * Example: /download-pdf?productIds=1,2,3
     */
    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadComparisonPdf(@RequestParam List<Long> productIds) {
        try {
            ComparisonResultDTO comparison = comparisonService.compareProducts(productIds);
            ByteArrayOutputStream pdfStream = pdfService.generateComparisonPdf(comparison);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Insurance_Comparison.pdf");

            return new ResponseEntity<>(pdfStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Convert InsuranceProduct entity to InsuranceProductComparisonDTO with calculated fields
     */
    private InsuranceProductComparisonDTO convertToComparisonDTO(InsuranceProduct product) {
        InsuranceProductComparisonDTO dto = new InsuranceProductComparisonDTO();

        // Map basic fields
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setPolicyType(product.getPolicyType());
        dto.setPremiumAmount(product.getPremiumAmount());
        dto.setCoverageLimit(product.getCoverageLimit());
        dto.setDurationMonths(product.getDurationMonths());
        dto.setDescription(product.getDescription());
        dto.setIsActive(true);

        // Calculate derived fields if premium amount exists and is greater than zero
        if (product.getPremiumAmount() != null &&
                product.getPremiumAmount().compareTo(BigDecimal.ZERO) > 0) {

            // Calculate cost per month
            if (product.getDurationMonths() != null && product.getDurationMonths() > 0) {
                BigDecimal costPerMonth = product.getPremiumAmount()
                        .divide(BigDecimal.valueOf(product.getDurationMonths()), 2, RoundingMode.HALF_UP);
                dto.setCostPerMonth(costPerMonth);
            }

            // Calculate coverage per dollar
            if (product.getCoverageLimit() != null) {
                BigDecimal coveragePerDollar = product.getCoverageLimit()
                        .divide(product.getPremiumAmount(), 2, RoundingMode.HALF_UP);
                dto.setCoveragePerDollar(coveragePerDollar);

                // Calculate value score and rating
                double valueScore = coveragePerDollar.doubleValue();
                dto.setValueScore(valueScore);

                // Assign rating based on value score
                if (valueScore > 100) {
                    dto.setValueRating("Best Value");
                } else if (valueScore > 50) {
                    dto.setValueRating("Good Value");
                } else {
                    dto.setValueRating("Standard");
                }
            }
        }

        return dto;
    }
}