package org.example.forsapidev.Controllers;

import org.example.forsapidev.Services.Interfaces.IPolicyPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;

@RestController
@AllArgsConstructor
@RequestMapping("/policy-pdf")
public class PolicyPdfController {

    private final IPolicyPdfService policyPdfService;

    /**
     * Generate and download PDF for a policy
     */
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/download/{policy-id}")
    public ResponseEntity<byte[]> downloadPolicyPdf(@PathVariable("policy-id") Long policyId) {
        try {
            ByteArrayOutputStream pdfStream = policyPdfService.generatePolicyPdf(policyId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Policy_" + policyId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("❌ Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Generate and view PDF in browser
     */
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/view/{policy-id}")
    public ResponseEntity<byte[]> viewPolicyPdf(@PathVariable("policy-id") Long policyId) {
        try {
            ByteArrayOutputStream pdfStream = policyPdfService.generatePolicyPdf(policyId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Policy_" + policyId + ".pdf");

            return new ResponseEntity<>(pdfStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("❌ Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}