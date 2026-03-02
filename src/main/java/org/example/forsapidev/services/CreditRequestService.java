package org.example.forsapidev.Services;

import org.example.forsapidev.DTO.CreditRequestCreateDTO;
import org.example.forsapidev.entities.CreditManagement.AmortizationType;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.RepaymentScheduleRepository;
import org.example.forsapidev.Services.amortization.AmortizationCalculatorService;
import org.example.forsapidev.Services.amortization.AmortizationResult;
import org.example.forsapidev.Services.scoring.CreditScoringService;
import org.example.forsapidev.Services.scoring.ScoringServiceException;
import org.example.forsapidev.Services.scoring.UnifiedCreditAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CreditRequestService {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestService.class);

    private final CreditRequestRepository creditRequestRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final InterestRateEngineService interestRateEngineService;
    private final AmortizationCalculatorService amortizationCalculatorService;
    private final CreditScoringService creditScoringService;
    private final UnifiedCreditAnalysisService unifiedCreditAnalysisService;
    private final GiftService giftService;
    private final AgentAssignmentService agentAssignmentService;

    public CreditRequestService(CreditRequestRepository creditRequestRepository,
                                RepaymentScheduleRepository repaymentScheduleRepository,
                                InterestRateEngineService interestRateEngineService,
                                AmortizationCalculatorService amortizationCalculatorService,
                                CreditScoringService creditScoringService,
                                UnifiedCreditAnalysisService unifiedCreditAnalysisService,
                                GiftService giftService,
                                AgentAssignmentService agentAssignmentService) {
        this.creditRequestRepository = creditRequestRepository;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.interestRateEngineService = interestRateEngineService;
        this.amortizationCalculatorService = amortizationCalculatorService;
        this.creditScoringService = creditScoringService;
        this.unifiedCreditAnalysisService = unifiedCreditAnalysisService;
        this.giftService = giftService;
        this.agentAssignmentService = agentAssignmentService;
    }

    // Create a credit request and generate repayment schedules automatically
    @Transactional
    public CreditRequest createCreditRequest(CreditRequest request) {
        logger.info("Création d'une nouvelle demande de crédit pour un montant de {}", request.getAmountRequested());

        if (request.getStatus() == null) request.setStatus(CreditStatus.SUBMITTED);
        if (request.getRequestDate() == null) request.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        if (request.getTypeCalcul() == null) request.setTypeCalcul(AmortizationType.AMORTISSEMENT_CONSTANT);
        if (request.getInterestRate() == null || request.getInterestRate() <= 0) {
            BigDecimal finalRatePercent = interestRateEngineService.computeAnnualRatePercent(
                    request.getRequestDate(), request.getDurationMonths(), null, null);
            request.setInterestRate(finalRatePercent.doubleValue());
        }

        // Sauvegarde initiale pour obtenir un ID
        CreditRequest savedRequest = creditRequestRepository.save(request);

        // Scoring IA automatique lors de la création
        try {
            logger.info("Lancement du scoring IA pour la demande de crédit ID={}", savedRequest.getId());
            creditScoringService.scoreCredit(savedRequest);

            // Mise à jour du statut en fonction du scoring
            savedRequest.setStatus(CreditStatus.UNDER_REVIEW);
            savedRequest = creditRequestRepository.save(savedRequest);

            logger.info("Demande de crédit créée avec succès - ID={}, Risque={}",
                       savedRequest.getId(),
                       savedRequest.getRiskLevel());
        } catch (ScoringServiceException e) {
            // Si le scoring échoue, on laisse la demande en SUBMITTED
            logger.warn("Le scoring IA a échoué pour la demande ID={} - Demande laissée en statut SUBMITTED pour revue manuelle : {}",
                       savedRequest.getId(), e.getMessage());
            // On ne bloque pas la création, l'agent pourra décider manuellement
        }

        return savedRequest;
    }

    /**
     * Crée une demande de crédit avec l'utilisateur authentifié
     * Cette méthode prend un DTO et l'utilisateur extrait du JWT
     */
    @Transactional
    public CreditRequest createCreditRequest(CreditRequestCreateDTO dto, User authenticatedUser) {
        logger.info("Création d'une nouvelle demande de crédit pour l'utilisateur {} avec montant {}",
                   authenticatedUser.getUsername(), dto.getAmountRequested());

        CreditRequest request = new CreditRequest();
        request.setAmountRequested(dto.getAmountRequested());
        request.setInterestRate(dto.getInterestRate());
        request.setDurationMonths(dto.getDurationMonths());
        request.setTypeCalcul(dto.getTypeCalcul());
        request.setUser(authenticatedUser);

        return createCreditRequest(request);
    }

    /**
     * Crée une demande de crédit avec rapport médical
     * Appelle l'API Python unifiée pour l'analyse complète (fraude + assurance)
     */
    @Transactional
    public CreditRequest createCreditRequestWithHealthReport(
            BigDecimal amountRequested,
            Integer durationMonths,
            String typeCalculStr,
            MultipartFile healthReport,
            User authenticatedUser) {

        logger.info("🚀 Création d'une demande de crédit avec rapport médical pour l'utilisateur {} avec montant {}",
                   authenticatedUser.getUsername(), amountRequested);

        // Création de la demande de crédit
        CreditRequest request = new CreditRequest();
        request.setAmountRequested(amountRequested);
        request.setDurationMonths(durationMonths);
        request.setUser(authenticatedUser);
        request.setStatus(CreditStatus.SUBMITTED);
        request.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());

        // Type de calcul
        try {
            request.setTypeCalcul(AmortizationType.valueOf(typeCalculStr));
        } catch (IllegalArgumentException e) {
            request.setTypeCalcul(AmortizationType.AMORTISSEMENT_CONSTANT);
        }

        // Calcul du taux d'intérêt de base (avant ajustement assurance)
        BigDecimal baseRate = interestRateEngineService.computeAnnualRatePercent(
                request.getRequestDate(), durationMonths, null, null);
        request.setInterestRate(baseRate.doubleValue());

        // Sauvegarde initiale
        CreditRequest savedRequest = creditRequestRepository.save(request);

        try {
            // Analyse unifiée : fraude + assurance via API Python
            logger.info("📡 Appel de l'API Python unifiée pour l'analyse crédit complète...");
            savedRequest = unifiedCreditAnalysisService.analyzeCredit(savedRequest, healthReport);

            // Ajustement du taux d'intérêt en fonction de l'assurance
            if (!savedRequest.getInsuranceIsReject() && savedRequest.getInsuranceRate() != null) {
                // Le taux d'intérêt final inclut le taux d'assurance
                double finalRate = baseRate.doubleValue() + savedRequest.getInsuranceRate().doubleValue();
                savedRequest.setInterestRate(finalRate);

                logger.info("💹 Taux d'intérêt ajusté : Base={}%, Assurance={}%, Final={}%",
                           baseRate, savedRequest.getInsuranceRate(), finalRate);
            }

            // Mise à jour du statut
            savedRequest.setStatus(CreditStatus.UNDER_REVIEW);
            savedRequest = creditRequestRepository.save(savedRequest);


            logger.info("✅ Demande de crédit créée avec succès - ID={}", savedRequest.getId());
            logger.info("   📊 Risque fraude : {} ({})",
                       savedRequest.getRiskLevel(), savedRequest.getIsRisky() ? "RISKY" : "SAFE");
            logger.info("   🏥 Assurance : {}",
                       savedRequest.getInsuranceIsReject() ? "REJETÉE" : "Approuvée - Taux " + savedRequest.getInsuranceRate() + "%");
            logger.info("   🎯 Décision globale : {}", savedRequest.getGlobalDecision());

            return savedRequest;

        } catch (Exception e) {
            logger.error("❌ Échec de l'analyse crédit unifiée : {}", e.getMessage(), e);
            // En cas d'échec, on laisse la demande en SUBMITTED pour revue manuelle
            savedRequest.setStatus(CreditStatus.SUBMITTED);
            creditRequestRepository.save(savedRequest);
            throw new RuntimeException("Erreur lors de l'analyse crédit : " + e.getMessage(), e);
        }
    }

    public Optional<CreditRequest> getById(Long id) { return creditRequestRepository.findById(id); }
    public List<CreditRequest> getAll() { return creditRequestRepository.findAll(); }

    @Transactional
    public CreditRequest updateCredit(Long id, CreditRequest update) {
        CreditRequest existing = creditRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Credit not found"));
        existing.setInterestRate(update.getInterestRate());
        existing.setDurationMonths(update.getDurationMonths());
        existing.setStatus(update.getStatus());
        existing.setAgentId(update.getAgentId());
        return creditRequestRepository.save(existing);
    }

    @Transactional
    public void deleteCredit(Long id) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByCreditRequestId(id);
        if (schedules != null && !schedules.isEmpty()) repaymentScheduleRepository.deleteAll(schedules);
        creditRequestRepository.deleteById(id);
    }

    /**
     * Valide et approuve une demande de crédit
     * WORKFLOW COMPLET :
     * 1. Vérifie le statut
     * 2. Si pas encore scoré, lance le scoring IA
     * 3. Passe en APPROVED
     * 4. Génère le tableau d'amortissement
     *
     * @param id ID du crédit à valider
     * @return le crédit validé
     */
    @Transactional
    public CreditRequest validateCredit(Long id) {
        logger.info("Validation de la demande de crédit ID={}", id);

        CreditRequest credit = creditRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));

        // Validation du statut
        if (credit.getStatus() == CreditStatus.APPROVED || credit.getStatus() == CreditStatus.ACTIVE) {
            throw new IllegalStateException("Le crédit est déjà validé");
        }

        // Si le crédit n'a pas encore été scoré, on le score maintenant
        if (credit.getIsRisky() == null) {
            try {
                logger.info("Scoring IA non effectué, lancement pour crédit ID={}", id);
                creditScoringService.scoreCredit(credit);
                creditRequestRepository.save(credit);
                logger.info("Scoring terminé : Risque={}",
                           credit.getRiskLevel());
            } catch (ScoringServiceException e) {
                logger.warn("Le scoring IA a échoué pour le crédit ID={} : {}", id, e.getMessage());
                // On continue quand même la validation (décision manuelle)
            }
        }

        // Passer en APPROVED
        credit.setStatus(CreditStatus.APPROVED);

        // Initialisation des champs si nécessaires
        if (credit.getRequestDate() == null) {
            credit.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (credit.getInterestRate() == null || credit.getInterestRate() <= 0) {
            BigDecimal finalRatePercent = interestRateEngineService.computeAnnualRatePercent(
                    credit.getRequestDate(), credit.getDurationMonths(), null, null);
            credit.setInterestRate(finalRatePercent.doubleValue());
        }
        if (credit.getTypeCalcul() == null) {
            credit.setTypeCalcul(AmortizationType.AMORTISSEMENT_CONSTANT);
        }

        CreditRequest saved = creditRequestRepository.save(credit);

        // Génération du tableau d'amortissement selon le type choisi
        generateRepaymentSchedule(saved);

        // Accumulation du gift (1.5% du capital)
        try {
            logger.info("🎁 Accumulation du gift pour le crédit ID={}", id);
            giftService.accumulateForCredit(saved);
        } catch (Exception e) {
            logger.warn("⚠️ Erreur lors de l'accumulation du gift : {}", e.getMessage());
            // On ne bloque pas la validation même si le gift échoue
        }

        logger.info("Crédit ID={} validé avec succès", id);
        return saved;
    }

    /**
     * Approuve une demande de crédit (décision de l'agent)
     * Le score IA a déjà été calculé lors de la création, l'agent prend la décision finale
     *
     * Cette méthode appelle validateCredit() pour tout faire en une seule fois
     *
     * @param id ID du crédit à approuver
     * @return le crédit approuvé
     */
    @Transactional
    public CreditRequest approveCredit(Long id) {
        logger.info("Approbation de la demande de crédit ID={} par l'agent", id);

        CreditRequest credit = creditRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crédit introuvable avec l'ID : " + id));

        // Vérification du statut actuel
        if (credit.getStatus() != CreditStatus.SUBMITTED && credit.getStatus() != CreditStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Seuls les crédits en statut SUBMITTED ou UNDER_REVIEW peuvent être approuvés");
        }

        logger.info("Crédit ID={} - Risque : {}",
                   id, credit.getRiskLevel());

        // Appel à validateCredit qui fait tout :
        // - Scoring si nécessaire
        // - Passage en APPROVED
        // - Génération des échéances
        CreditRequest approved = validateCredit(id);

        // Libérer l'agent après approbation
        try {
            if (approved.getAgentId() != null) {
                agentAssignmentService.releaseAgent(approved.getAgentId());
                logger.info("Agent {} libéré après approbation du crédit ID={}", approved.getAgentId(), id);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Erreur lors de la libération de l'agent après approbation : {}", e.getMessage());
            // Ne pas bloquer l'approbation si la libération de l'agent échoue
        }

        return approved;
    }

    /**
     * Rejette une demande de crédit (décision de l'agent)
     */
    @Transactional
    public CreditRequest rejectCredit(Long id, String reason) {
        logger.info("Rejet de la demande de crédit ID={}", id);

        CreditRequest credit = creditRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crédit introuvable avec l'ID : " + id));

        // Vérification du statut actuel
        if (credit.getStatus() != CreditStatus.SUBMITTED && credit.getStatus() != CreditStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Seuls les crédits en statut SUBMITTED ou UNDER_REVIEW peuvent être rejetés");
        }

        // L'agent rejette : statut final
        credit.setStatus(CreditStatus.REJECTED);
        CreditRequest rejected = creditRequestRepository.save(credit);

        // Libérer l'agent après rejet
        try {
            if (rejected.getAgentId() != null) {
                agentAssignmentService.releaseAgent(rejected.getAgentId());
                logger.info("Agent {} libéré après rejet du crédit ID={}", rejected.getAgentId(), id);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Erreur lors de la libération de l'agent après rejet : {}", e.getMessage());
            // Ne pas bloquer le rejet si la libération de l'agent échoue
        }

        logger.info("Crédit ID={} rejeté par l'agent - Risque : {}, Raison : {}",
                   id, credit.getRiskLevel(), reason);

        return rejected;
    }

    /**
     * Génère le tableau d'amortissement selon le type de calcul choisi
     * Utilise le pattern Strategy via AmortizationCalculatorService
     */
    @Transactional
    public List<RepaymentSchedule> generateRepaymentSchedule(CreditRequest credit) {
        if (credit.getDurationMonths() == null || credit.getDurationMonths() <= 0) {
            return new ArrayList<>();
        }

        BigDecimal principal = credit.getAmountRequested();
        BigDecimal annualRatePercent = BigDecimal.valueOf(credit.getInterestRate());
        int durationMonths = credit.getDurationMonths();
        AmortizationType type = credit.getTypeCalcul();

        if (type == null) {
            type = AmortizationType.AMORTISSEMENT_CONSTANT;
        }

        // Utilisation du service de calcul avec Strategy Pattern
        AmortizationResult result = amortizationCalculatorService.calculateSchedule(
                type, principal, annualRatePercent, durationMonths);

        // Conversion des périodes en entités RepaymentSchedule
        List<RepaymentSchedule> schedules = new ArrayList<>();
        java.time.LocalDate startDate = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate();

        for (AmortizationResult.MonthlyPeriod period : result.getPeriods()) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setDueDate(startDate.plusMonths(period.getMonthNumber()));
            schedule.setPrincipalPart(period.getPrincipalPayment());
            schedule.setInterestPart(period.getInterestPayment());
            schedule.setTotalAmount(period.getTotalPayment());
            schedule.setRemainingBalance(period.getRemainingBalance());
            schedule.setStatus(RepaymentStatus.PENDING);
            schedule.setCreditRequest(credit);
            schedules.add(schedule);
        }

        return repaymentScheduleRepository.saveAll(schedules);
    }


    @Transactional
    public void onRepaymentPaid(RepaymentSchedule schedule) {
        Long creditId = schedule.getCreditRequest().getId();
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByCreditRequestIdOrderByDueDateAsc(creditId);
        BigDecimal remainingTotal = BigDecimal.ZERO;
        boolean anyPending = false;
        for (RepaymentSchedule s : schedules) {
            if (s.getStatus() != RepaymentStatus.PAID) {
                anyPending = true;
                remainingTotal = remainingTotal.add(s.getRemainingBalance() == null ? BigDecimal.ZERO : s.getRemainingBalance());
            }
        }
        CreditRequest credit = creditRequestRepository.findById(creditId).orElseThrow();
        if (!anyPending) credit.setStatus(CreditStatus.REPAID); else credit.setStatus(CreditStatus.ACTIVE);
        creditRequestRepository.save(credit);
    }
}
