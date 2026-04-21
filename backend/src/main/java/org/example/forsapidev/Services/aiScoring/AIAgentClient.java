package org.example.forsapidev.Services.aiScoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AIAgentClient {

    @Value("${ai.agent.url:http://localhost:5000}")
    private String aiAgentUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Appelle POST http://localhost:5000/calculate-score
     * avec toutes les données du formulaire Angular.
     */
    /**
     * Transmet un fichier image/PDF au service OCR Python.
     * Appelle POST http://localhost:5000/verify-document
     */
    public Map<String, Object> verifyDocument(String documentType, MultipartFile file) {
        try {
            String url = aiAgentUrl + "/verify-document";

            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "doc.jpg";

            ByteArrayResource fileResource = new ByteArrayResource(bytes) {
                @Override public String getFilename() { return filename; }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("document_type", documentType.toUpperCase());
            body.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("OCR {} → {}", documentType, response.getBody());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur OCR document {} : {}", documentType, e.getMessage());
            throw new RuntimeException("Erreur OCR : " + e.getMessage());
        }
    }

    /**
     * Calcule le score IA en envoyant les données du formulaire.
     */
    public Map<String, Object> calculateScore(
            Long clientId,
            Double monthlySalary,
            Boolean stegPaidOnTime,
            Boolean sondePaidOnTime,
            Boolean cinVerified) {
        try {
            String url = aiAgentUrl + "/calculate-score";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("client_id", clientId);
            requestBody.put("monthly_salary", monthlySalary != null ? monthlySalary : 0.0);
            requestBody.put("steg_paid_on_time", stegPaidOnTime != null ? stegPaidOnTime : false);
            requestBody.put("sonede_paid_on_time", sondePaidOnTime != null ? sondePaidOnTime : false);
            requestBody.put("cin_verified", cinVerified != null ? cinVerified : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Appel Python AI → {} | client={} salary={} steg={} sonede={} cin={}",
                    url, clientId, monthlySalary, stegPaidOnTime, sondePaidOnTime, cinVerified);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("Score reçu pour client {} : {}", clientId, response.getBody());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur appel Python AI : {}", e.getMessage());
            throw new RuntimeException("Service IA indisponible : " + e.getMessage());
        }
    }
}
