package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.RepaymentScheduleRepository;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.LineType;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des pénalités pour retards de paiement
 * Règle : 200 DT de pénalité fixe ajoutée comme dernière ligne du schedule
 */
@Service
public class PenaltyService {

    private static final Logger logger = LoggerFactory.getLogger(PenaltyService.class);

    /**
     * Montant fixe de pénalité pour retard (200 DT)
     */
    private static final BigDecimal PENALTY_AMOUNT = new BigDecimal("200.00");

    private final RepaymentScheduleRepository scheduleRepository;
    private final CreditRequestRepository creditRequestRepository;

    @Autowired
    public PenaltyService(RepaymentScheduleRepository scheduleRepository,
                         CreditRequestRepository creditRequestRepository) {
        this.scheduleRepository = scheduleRepository;
        this.creditRequestRepository = creditRequestRepository;
    }

    /**
     * Vérifie les retards et ajoute des pénalités si nécessaire
     * Exécuté quotidiennement à 1h du matin
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void checkOverduePaymentsAndApplyPenalties() {
        logger.info("🔍 Début de la vérification des retards de paiement...");

        LocalDate today = LocalDate.now();

        // Trouver toutes les échéances en retard (dueDate < today et status != PAID)
        List<RepaymentSchedule> overdueSchedules = scheduleRepository
                .findByDueDateBeforeAndStatusNot(today, RepaymentStatus.PAID);

        logger.info("Nombre d'échéances en retard trouvées : {}", overdueSchedules.size());

        for (RepaymentSchedule schedule : overdueSchedules) {
            applyPenaltyForOverdueSchedule(schedule);
        }

        logger.info("✅ Vérification des retards terminée");
    }

    /**
     * Applique une pénalité pour une échéance en retard
     */
    @Transactional
    public void applyPenaltyForOverdueSchedule(RepaymentSchedule overdueSchedule) {
        if (overdueSchedule == null || overdueSchedule.getStatus() == RepaymentStatus.PAID) {
            return;
        }

        CreditRequest creditRequest = overdueSchedule.getCreditRequest();
        if (creditRequest == null) {
            logger.error("CreditRequest null pour schedule {}", overdueSchedule.getId());
            return;
        }

        // Vérifier si une pénalité n'existe pas déjà pour ce crédit
        List<RepaymentSchedule> existingPenalties = scheduleRepository
                .findByCreditRequestIdAndLineType(creditRequest.getId(), LineType.PENALTY);

        if (!existingPenalties.isEmpty()) {
            logger.info("Pénalité déjà appliquée pour le crédit {}", creditRequest.getId());
            return;
        }

        // Calculer le nombre de jours de retard
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                overdueSchedule.getDueDate(), LocalDate.now());

        if (daysOverdue <= 0) {
            return; // Pas de retard
        }

        logger.warn("⚠️ Retard de {} jours détecté pour échéance {} du crédit {}",
                daysOverdue, overdueSchedule.getId(), creditRequest.getId());

        // Créer une ligne de pénalité
        RepaymentSchedule penaltyLine = new RepaymentSchedule();
        penaltyLine.setCreditRequest(creditRequest);
        penaltyLine.setLineType(LineType.PENALTY);
        penaltyLine.setDueDate(LocalDate.now());
        penaltyLine.setStatus(RepaymentStatus.PENDING);

        // Montants pour pénalité
        penaltyLine.setPrincipalPart(BigDecimal.ZERO);
        penaltyLine.setInterestPart(BigDecimal.ZERO);
        penaltyLine.setTotalAmount(PENALTY_AMOUNT);

        // Remaining balance = ancien remaining balance + pénalité
        BigDecimal newRemainingBalance = overdueSchedule.getRemainingBalance()
                .add(PENALTY_AMOUNT);
        penaltyLine.setRemainingBalance(newRemainingBalance);

        // Sauvegarder la pénalité
        scheduleRepository.save(penaltyLine);

        // Mettre à jour le solde restant du crédit
        creditRequest.setRemainingBalance(newRemainingBalance);
        creditRequestRepository.save(creditRequest);

        logger.warn("💰 Pénalité de {} DT appliquée au crédit {} - Nouveau solde : {}",
                PENALTY_AMOUNT, creditRequest.getId(), newRemainingBalance);

        // TODO: Envoyer notification au client
        // notificationService.sendPenaltyNotification(creditRequest.getUser(), PENALTY_AMOUNT, daysOverdue);
    }

    /**
     * Applique manuellement une pénalité pour un crédit spécifique
     */
    @Transactional
    public RepaymentSchedule applyPenaltyForCredit(Long creditRequestId) {
        CreditRequest creditRequest = creditRequestRepository.findById(creditRequestId)
                .orElseThrow(() -> new RuntimeException("CreditRequest not found: " + creditRequestId));

        // Vérifier si une pénalité existe déjà
        List<RepaymentSchedule> existingPenalties = scheduleRepository
                .findByCreditRequestIdAndLineType(creditRequestId, LineType.PENALTY);

        if (!existingPenalties.isEmpty()) {
            logger.warn("Pénalité déjà existante pour le crédit {}", creditRequestId);
            return existingPenalties.get(0);
        }

        // Trouver la dernière échéance pour le remaining balance
        List<RepaymentSchedule> schedules = scheduleRepository
                .findByCreditRequestIdOrderByDueDateDesc(creditRequestId);

        if (schedules.isEmpty()) {
            throw new RuntimeException("Aucun schedule trouvé pour le crédit " + creditRequestId);
        }

        RepaymentSchedule lastSchedule = schedules.get(0);

        // Créer la pénalité
        RepaymentSchedule penaltyLine = new RepaymentSchedule();
        penaltyLine.setCreditRequest(creditRequest);
        penaltyLine.setLineType(LineType.PENALTY);
        penaltyLine.setDueDate(LocalDate.now());
        penaltyLine.setStatus(RepaymentStatus.PENDING);
        penaltyLine.setPrincipalPart(BigDecimal.ZERO);
        penaltyLine.setInterestPart(BigDecimal.ZERO);
        penaltyLine.setTotalAmount(PENALTY_AMOUNT);

        BigDecimal newRemainingBalance = lastSchedule.getRemainingBalance().add(PENALTY_AMOUNT);
        penaltyLine.setRemainingBalance(newRemainingBalance);

        scheduleRepository.save(penaltyLine);

        // Mettre à jour le crédit
        creditRequest.setRemainingBalance(newRemainingBalance);
        creditRequestRepository.save(creditRequest);

        logger.info("Pénalité manuelle appliquée au crédit {}", creditRequestId);

        return penaltyLine;
    }

    /**
     * Récupère toutes les pénalités d'un crédit
     */
    public List<RepaymentSchedule> getPenaltiesForCredit(Long creditRequestId) {
        return scheduleRepository.findByCreditRequestIdAndLineType(creditRequestId, LineType.PENALTY);
    }

    /**
     * Compte le nombre total de pénalités
     */
    public long countAllPenalties() {
        return scheduleRepository.countByLineType(LineType.PENALTY);
    }
}

