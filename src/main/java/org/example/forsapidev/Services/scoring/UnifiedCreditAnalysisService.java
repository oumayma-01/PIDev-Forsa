package org.example.forsapidev.Services.scoring;

import org.example.forsapidev.Services.AgentAssignmentService;
import org.example.forsapidev.Services.insurance.HealthReportStorageService;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.CreditManagement.RiskLevel;
import org.example.forsapidev.entities.UserManagement.Agent;
import org.example.forsapidev.payload.request.UnifiedCreditAnalysisRequestDto;
import org.example.forsapidev.payload.response.UnifiedCreditAnalysisResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service unifié pour l'analyse crédit complète :
 * - Scoring de risque de fraude
 * - Analyse du rapport médical et calcul du taux d'assurance
 * - Génération du rapport PDF global
 */
@Service
public class UnifiedCreditAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedCreditAnalysisService.class);

    private final UnifiedCreditAnalysisClient analysisClient;
    private final FeatureCalculationService featureCalculationService;
    private final HealthReportStorageService healthReportStorageService;
    private final AgentAssignmentService agentAssignmentService;

    public UnifiedCreditAnalysisService(
            UnifiedCreditAnalysisClient analysisClient,
            FeatureCalculationService featureCalculationService,
            HealthReportStorageService healthReportStorageService,
            AgentAssignmentService agentAssignmentService) {
        this.analysisClient = analysisClient;
        this.featureCalculationService = featureCalculationService;
        this.healthReportStorageService = healthReportStorageService;
        this.agentAssignmentService = agentAssignmentService;
    }

    /**
     * Analyse complète d'une demande de crédit avec rapport médical
     *
     * @param creditRequest Demande de crédit
     * @param healthReportFile Fichier PDF du rapport médical
     * @return CreditRequest mis à jour avec tous les résultats
     */
    @Transactional
    public CreditRequest analyzeCredit(CreditRequest creditRequest, MultipartFile healthReportFile) {

        logger.info("🚀 Démarrage de l'analyse crédit unifiée pour ID={}", creditRequest.getId());

        // Validation
        if (creditRequest.getUser() == null) {
            throw new UnifiedAnalysisException("Le crédit doit être associé à un utilisateur");
        }

        Long userId = creditRequest.getUser().getId();
        String clientId = "CLIENT_" + userId + "_" + creditRequest.getId();

        // 1. Stockage du rapport médical
        logger.info("📄 Stockage du rapport médical...");
        String storedFilename = healthReportStorageService.storeHealthReport(
                healthReportFile,
                userId,
                creditRequest.getId()
        );

        creditRequest.setHealthReportPath(storedFilename);
        creditRequest.setOriginalHealthReportFilename(healthReportFile.getOriginalFilename());

        try {
            // 2. Calcul des features pour l'analyse de fraude
            logger.info("🔢 Calcul des features client...");
            UnifiedCreditAnalysisRequestDto clientData = buildClientDataFromCredit(creditRequest, userId, clientId);

            // 3. Récupération du fichier stocké
            File medicalFile = healthReportStorageService.getFilePath(storedFilename).toFile();

            // 4. Appel à l'API Python unifiée
            String requestId = "REQ_" + clientId + "_" + System.currentTimeMillis();
            UnifiedCreditAnalysisResponseDto analysisResult = analysisClient.analyzeCredit(
                    clientData,
                    medicalFile,
                    requestId
            );

            // 5. Mise à jour du CreditRequest avec les résultats

            // --- Résultats de fraude ---
            creditRequest.setIsRisky(analysisResult.getFraudRiskProbability() > 0.7);
            creditRequest.setRiskLevel(mapRiskCategory(analysisResult.getFraudRiskCategory()));
            creditRequest.setFraudReportPath(analysisResult.getFraudReportPath());

            // --- Résultats d'assurance ---
            creditRequest.setInsuranceIsReject(analysisResult.getInsuranceIsReject());
            creditRequest.setInsuranceRating(analysisResult.getInsuranceRating());
            creditRequest.setInsuranceScoringReport(analysisResult.getInsuranceScoringReport());

            if (!analysisResult.getInsuranceIsReject() && analysisResult.getInsuranceRate() != null) {
                // Assurance approuvée - calculer le montant
                BigDecimal insuranceRate = BigDecimal.valueOf(analysisResult.getInsuranceRate());
                BigDecimal insuranceAmount = creditRequest.getAmountRequested()
                        .multiply(insuranceRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);

                creditRequest.setInsuranceRate(insuranceRate);
                creditRequest.setInsuranceAmount(insuranceAmount);

                logger.info("💰 Assurance calculée : Taux={}%, Montant={}", insuranceRate, insuranceAmount);
            } else {
                // Assurance rejetée
                creditRequest.setInsuranceRate(null);
                creditRequest.setInsuranceAmount(null);

                logger.warn("⚠️ Assurance rejetée : {}", analysisResult.getInsuranceScoringReport());
            }

            // --- Résultats globaux ---
            creditRequest.setGlobalDecision(analysisResult.getGlobalDecision());
            creditRequest.setGlobalPdfPath(analysisResult.getGlobalReportPdfPath());
            creditRequest.setScoredAt(LocalDateTime.now());

            logger.info("✅ Analyse crédit unifiée terminée avec succès :");
            logger.info("   📊 Risque fraude : {} ({})",
                       creditRequest.getRiskLevel(), creditRequest.getIsRisky() ? "RISKY" : "SAFE");
            logger.info("   🏥 Assurance : {}",
                       creditRequest.getInsuranceIsReject() ? "REJETÉE" : "Taux " + creditRequest.getInsuranceRate() + "%");
            logger.info("   🎯 Décision globale : {}", creditRequest.getGlobalDecision());

            // Mise à jour du statut
            // Ne passe en UNDER_REVIEW que si un agent est assigné
            Agent assignedAgent = agentAssignmentService.assignCreditRequestToAgent(creditRequest);
            if (assignedAgent != null) {
                creditRequest.setStatus(CreditStatus.UNDER_REVIEW);
                logger.info("✅ Crédit assigné à l'agent {} - Statut : UNDER_REVIEW", assignedAgent.getId());
            } else {
                creditRequest.setStatus(CreditStatus.SUBMITTED);
                logger.info("⚠️ Aucun agent disponible - Crédit reste en SUBMITTED");
            }

            return creditRequest;

        } catch (UnifiedCreditAnalysisClient.UnifiedAnalysisException e) {
            // En cas d'erreur, supprimer le fichier uploadé
            healthReportStorageService.deleteHealthReport(storedFilename);
            creditRequest.setHealthReportPath(null);
            creditRequest.setOriginalHealthReportFilename(null);

            logger.error("❌ Échec de l'analyse unifiée : {}", e.getMessage());
            throw new UnifiedAnalysisException("Erreur lors de l'analyse crédit : " + e.getMessage(), e);
        }
    }

    /**
     * Construit le DTO de données client à partir du CreditRequest
     */
    private UnifiedCreditAnalysisRequestDto buildClientDataFromCredit(
            CreditRequest creditRequest,
            Long userId,
            String clientId) {

        // Utiliser le service de calcul de features existant
        var scoringRequest = featureCalculationService.calculateFeatures(creditRequest, userId);

        UnifiedCreditAnalysisRequestDto clientData = new UnifiedCreditAnalysisRequestDto();
        clientData.setClientId(clientId);
        clientData.setAvgDelayDays(scoringRequest.getAvgDelayDays());
        clientData.setPaymentInstability(scoringRequest.getPaymentInstability());
        clientData.setCreditUtilization(0.0);
        clientData.setMonthlyTransactionCount(scoringRequest.getMonthlyTransactionCount());
        clientData.setTransactionAmountStd(scoringRequest.getTransactionAmountStd());
        clientData.setHighRiskCountryTransaction(scoringRequest.getHighRiskCountryTransaction());
        clientData.setUnusualNightTransaction(scoringRequest.getUnusualNightTransaction());
        clientData.setAddressChanged(scoringRequest.getAddressChanged());
        clientData.setPhoneChanged(scoringRequest.getPhoneChanged());
        clientData.setEmailChanged(scoringRequest.getEmailChanged());
        clientData.setCountryChanged(scoringRequest.getCountryChanged());
        clientData.setIncomeChangePercentage(scoringRequest.getIncomeChangePercentage());
        clientData.setEmploymentChanged(scoringRequest.getEmploymentChanged());
        clientData.setRecentCreditRequests(scoringRequest.getRecentCreditRequests());

        return clientData;
    }

    /**
     * Convertit la catégorie de risque Python en enum RiskLevel
     */
    private RiskLevel mapRiskCategory(String fraudRiskCategory) {
        if (fraudRiskCategory == null) {
            return RiskLevel.MEDIUM;
        }

        return switch (fraudRiskCategory.toUpperCase()) {
            case "LOW" -> RiskLevel.LOW;
            case "MEDIUM" -> RiskLevel.MEDIUM;
            case "HIGH" -> RiskLevel.HIGH;
            default -> RiskLevel.MEDIUM;
        };
    }

    /**
     * Exception personnalisée
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
