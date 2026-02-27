package org.example.forsapidev.Controllers;

import org.example.forsapidev.DTO.ComparisonResultDTO;
import org.example.forsapidev.Services.Interfaces.IComparisonPdfService;
import org.example.forsapidev.Services.Interfaces.IInsuranceProductComparisonService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/product-comparison")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductComparisonController {

    private final IInsuranceProductComparisonService comparisonService;
    private final IComparisonPdfService pdfService;

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}