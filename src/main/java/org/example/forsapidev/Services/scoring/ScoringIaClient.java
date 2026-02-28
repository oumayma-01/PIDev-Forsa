package org.example.forsapidev.Services.scoring;

import org.example.forsapidev.payload.request.ScoringRequestDto;
import org.example.forsapidev.payload.response.ScoringResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Client technique pour communiquer avec le service IA de scoring
 * Responsabilité : faire l'appel HTTP vers l'API IA et gérer les erreurs réseau
 */
@Component
public class ScoringIaClient {

    private static final Logger logger = LoggerFactory.getLogger(ScoringIaClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String predictPath;
    private final boolean enabled;

    public ScoringIaClient(
            @Qualifier("scoringRestTemplate") RestTemplate restTemplate,
            @Value("${ai.scoring.base-url}") String baseUrl,
            @Value("${ai.scoring.predict-path}") String predictPath,
            @Value("${ai.scoring.enabled:true}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.predictPath = predictPath;
        this.enabled = enabled;
    }

    /**
     * Appelle l'API IA pour obtenir un score de risque
     *
     * @param request les features du crédit
     * @return la réponse de l'IA contenant le score et le niveau de risque
     * @throws ScoringServiceException en cas d'erreur
     */
    public ScoringResponseDto predict(ScoringRequestDto request) {
        if (!enabled) {
            logger.warn("Service de scoring IA désactivé - retour d'un score neutre");
            return new ScoringResponseDto(0.5, false);
        }

        String url = baseUrl + predictPath;

        logger.info("Appel du service IA de scoring sur {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ScoringRequestDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ScoringResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ScoringResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Score IA reçu avec succès : score={}, risky={}",
                           response.getBody().getScore(),
                           response.getBody().isRisky());
                return response.getBody();
            } else {
                logger.error("Réponse inattendue du service IA : status={}", response.getStatusCode());
                throw new ScoringServiceException("Réponse inattendue du service IA");
            }

        } catch (HttpClientErrorException e) {
            logger.error("Erreur client lors de l'appel au service IA (4xx) : {}", e.getMessage());
            throw new ScoringServiceException("Erreur de validation des données pour le scoring IA", e);
        } catch (HttpServerErrorException e) {
            logger.error("Erreur serveur du service IA (5xx) : {}", e.getMessage());
            throw new ScoringServiceException("Le service IA rencontre une erreur interne", e);
        } catch (ResourceAccessException e) {
            logger.error("Impossible de joindre le service IA sur {} : {}", url, e.getMessage());
            throw new ScoringServiceException("Service IA indisponible - impossible de se connecter", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'appel au service IA : {}", e.getMessage(), e);
            throw new ScoringServiceException("Erreur inattendue lors du scoring IA", e);
        }
    }
}

