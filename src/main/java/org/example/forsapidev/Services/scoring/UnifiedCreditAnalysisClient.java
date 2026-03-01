package org.example.forsapidev.Services.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.forsapidev.payload.request.UnifiedCreditAnalysisRequestDto;
import org.example.forsapidev.payload.response.UnifiedCreditAnalysisResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Client pour communiquer avec l'API Python unifiée (port 8000)
 * Envoie les données client + PDF médical et reçoit :
 * - Scoring de fraude
 * - Taux d'assurance
 * - Rapport PDF global
 */
@Service
public class UnifiedCreditAnalysisClient {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedCreditAnalysisClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String unifiedApiUrl;

    public UnifiedCreditAnalysisClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${ai.scoring.endpoint:http://localhost:8000/credit-full-analysis}") String unifiedApiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.unifiedApiUrl = unifiedApiUrl;
    }

    /**
     * Analyse complète du crédit : fraude + assurance
     *
     * @param clientData Données du client pour l'analyse de fraude
     * @param medicalFile Fichier PDF du rapport médical
     * @param requestId ID unique de la requête
     * @return Réponse unifiée avec scoring de fraude et taux d'assurance
     */
    public UnifiedCreditAnalysisResponseDto analyzeCredit(
            UnifiedCreditAnalysisRequestDto clientData,
            File medicalFile,
            String requestId) {

        logger.info("📤 Envoi de l'analyse crédit unifiée à l'API Python : {}", unifiedApiUrl);
        logger.info("📋 Client ID: {}, Request ID: {}", clientData.getClientId(), requestId);

        try {
            // Préparation de la requête multipart
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Conversion des données client en JSON string
            String clientDataJson = objectMapper.writeValueAsString(clientData);
            body.add("client_data_json", clientDataJson);

            // Ajout du fichier médical
            body.add("medical_file", new FileSystemResource(medicalFile));

            // Ajout du request ID
            if (requestId != null && !requestId.isEmpty()) {
                body.add("request_id", requestId);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Appel à l'API unifiée
            ResponseEntity<UnifiedCreditAnalysisResponseDto> response = restTemplate.exchange(
                    unifiedApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    UnifiedCreditAnalysisResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                UnifiedCreditAnalysisResponseDto result = response.getBody();

                logger.info("✅ Analyse unifiée terminée avec succès :");
                logger.info("   📊 Fraude : {} (prob: {})",
                           result.getFraudRiskCategory(), result.getFraudRiskProbability());
                logger.info("   🏥 Assurance : {} (taux: {}%)",
                           result.getInsuranceIsReject() ? "REJETÉ" : result.getInsuranceRating(),
                           result.getInsuranceRate());
                logger.info("   📄 PDF global : {}", result.getGlobalReportPdfPath());
                logger.info("   🎯 Décision : {}", result.getGlobalDecision());

                return result;
            } else {
                throw new UnifiedAnalysisException("Réponse invalide de l'API unifiée");
            }

        } catch (HttpClientErrorException e) {
            logger.error("❌ Erreur client lors de l'appel à l'API unifiée ({}) : {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnifiedAnalysisException(
                "L'API unifiée a rejeté la requête : " + e.getResponseBodyAsString(), e);

        } catch (HttpServerErrorException e) {
            logger.error("❌ Erreur serveur de l'API unifiée ({}) : {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnifiedAnalysisException(
                "L'API unifiée a rencontré une erreur : " + e.getResponseBodyAsString(), e);

        } catch (ResourceAccessException e) {
            logger.error("❌ Impossible de joindre l'API unifiée sur {} : {}",
                        unifiedApiUrl, e.getMessage());
            throw new UnifiedAnalysisException(
                "Service d'analyse indisponible - impossible de se connecter à " + unifiedApiUrl, e);

        } catch (Exception e) {
            logger.error("❌ Erreur inattendue lors de l'appel à l'API unifiée", e);
            throw new UnifiedAnalysisException("Erreur lors de l'analyse crédit unifiée", e);
        }
    }

    /**
     * Exception personnalisée pour les erreurs de l'API unifiée
     */
    public static class UnifiedAnalysisException extends RuntimeException {
        public UnifiedAnalysisException(String message) {
            super(message);
        }

        public UnifiedAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

