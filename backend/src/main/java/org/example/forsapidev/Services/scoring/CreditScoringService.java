package org.example.forsapidev.Services.scoring;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.RiskLevel;
import org.example.forsapidev.payload.request.ScoringRequestDto;
import org.example.forsapidev.payload.response.ScoringResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service métier de scoring crédit
 * Responsabilité :
 * - Construire les features à partir d'un crédit
 * - Appeler l'IA via le client
 * - Interpréter le résultat et déterminer le niveau de risque
 */
@Service
public class CreditScoringService {

    private static final Logger logger = LoggerFactory.getLogger(CreditScoringService.class);

    private final ScoringIaClient scoringIaClient;
    private final FeatureCalculationService featureCalculationService;
    private final double riskThreshold;

    public CreditScoringService(
            ScoringIaClient scoringIaClient,
            FeatureCalculationService featureCalculationService,
            @Value("${ai.scoring.risk-threshold:0.7}") double riskThreshold) {
        this.scoringIaClient = scoringIaClient;
        this.featureCalculationService = featureCalculationService;
        this.riskThreshold = riskThreshold;
    }

    /**
     * Score un crédit en utilisant l'IA et met à jour les champs de risque
     *
     * @param creditRequest la demande de crédit à scorer
     * @return l'objet ScoringResult contenant isRisky et riskLevel
     */
    public ScoringResult scoreCredit(CreditRequest creditRequest) {
        logger.info("Démarrage du scoring pour le crédit ID={}", creditRequest.getId());

        // Récupération du userId (obligatoire pour le calcul des features)
        if (creditRequest.getUser() == null) {
            logger.warn("Crédit ID={} n'a pas d'utilisateur associé - scoring impossible", creditRequest.getId());
            throw new ScoringServiceException("Impossible de scorer un crédit sans utilisateur associé");
        }

        Long userId = creditRequest.getUser().getId();

        // Construction des features à partir des données réelles via FeatureCalculationService
        ScoringRequestDto features = featureCalculationService.calculateFeatures(creditRequest, userId);

        // Appel à l'IA
        ScoringResponseDto iaResponse;
        try {
            iaResponse = scoringIaClient.predict(features);
        } catch (ScoringServiceException e) {
            logger.error("Échec du scoring IA pour le crédit ID={} : {}",
                        creditRequest.getId(), e.getMessage());
            throw e;
        }

        // L'IA retourne déjà tout : risky et risk_level
        boolean isRisky = iaResponse.isRisky();

        // Conversion du risk_level string en enum (si fourni par l'IA)
        RiskLevel riskLevel = determineRiskLevelFromIa(iaResponse.getRiskLevel(), iaResponse.getScore());

        // Mise à jour du crédit avec les résultats du scoring
        creditRequest.setIsRisky(isRisky);
        creditRequest.setRiskLevel(riskLevel);
        creditRequest.setScoredAt(LocalDateTime.now());

        logger.info("Scoring terminé pour crédit ID={} : risky={}, level={}",
                   creditRequest.getId(), isRisky, riskLevel);

        return new ScoringResult(isRisky, riskLevel);
    }

    /**
     * Convertit le risk_level string retourné par l'IA en enum RiskLevel
     * Si l'IA ne retourne pas de risk_level, on le déduit du score
     *
     * @param iaRiskLevel niveau de risque retourné par l'IA (peut être null)
     * @param score score retourné par l'IA (0.0 à 1.0)
     * @return niveau de risque LOW/MEDIUM/HIGH
     */
    private RiskLevel determineRiskLevelFromIa(String iaRiskLevel, double score) {
        // Si l'IA nous donne déjà le risk_level, on l'utilise
        if (iaRiskLevel != null && !iaRiskLevel.isEmpty()) {
            try {
                return RiskLevel.valueOf(iaRiskLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Niveau de risque IA invalide '{}', calcul basé sur le score", iaRiskLevel);
            }
        }

        // Sinon, on déduit du score (fallback)
        if (score < 0.3) {
            return RiskLevel.LOW;
        } else if (score < 0.7) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.HIGH;
        }
    }

    /**
     * Classe interne pour encapsuler le résultat du scoring
     */
    public static class ScoringResult {
        private final boolean risky;
        private final RiskLevel riskLevel;

        public ScoringResult(boolean risky, RiskLevel riskLevel) {
            this.risky = risky;
            this.riskLevel = riskLevel;
        }


        public boolean isRisky() {
            return risky;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
    }
}







