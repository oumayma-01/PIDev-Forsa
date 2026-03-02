package org.example.forsapidev.Services.scoring;

import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.RepaymentScheduleRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.payload.request.ScoringRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de calcul des features pour le modèle IA de scoring
 * Calcule toutes les features nécessaires à partir des données réelles en base
 */
@Service
public class FeatureCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureCalculationService.class);

    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final CreditRequestRepository creditRequestRepository;

    public FeatureCalculationService(RepaymentScheduleRepository repaymentScheduleRepository,
                                    CreditRequestRepository creditRequestRepository) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.creditRequestRepository = creditRequestRepository;
    }

    /**
     * Calcule toutes les features pour une demande de crédit donnée
     */
    public ScoringRequestDto calculateFeatures(CreditRequest creditRequest, Long userId) {
        logger.info("Calcul des features pour crédit ID={}, userId={}", creditRequest.getId(), userId);

        ScoringRequestDto features = new ScoringRequestDto();

        // 1. Features basées sur l'historique de paiements
        features.setAvgDelayDays(calculateAverageDelayDays(userId));
        features.setPaymentInstability(calculatePaymentInstability(userId));

        // 2. Features basées sur les transactions (TODO: nécessite entité Transaction complète)
        features.setMonthlyTransactionCount(calculateMonthlyTransactionCount(userId));
        features.setTransactionAmountStd(calculateTransactionAmountStd(userId));
        features.setHighRiskCountryTransaction(detectHighRiskCountryTransactions(userId));
        features.setUnusualNightTransaction(detectUnusualNightTransactions(userId));

        // 4. Features basées sur les changements de profil (TODO: nécessite historique profil)
        features.setAddressChanged(detectAddressChange(userId));
        features.setPhoneChanged(detectPhoneChange(userId));
        features.setEmailChanged(detectEmailChange(userId));
        features.setCountryChanged(detectCountryChange(userId));

        // 5. Features basées sur le revenu
        // 6. Features basées sur le comportement de demandes de crédit
        features.setRecentCreditRequests(calculateRecentCreditRequests(userId));

        logger.info("Features calculées : avgDelay={}, instability={}, recentCreditRequests={}",
                   features.getAvgDelayDays(),
                   features.getPaymentInstability(),
                   features.getRecentCreditRequests());

        return features;
    }

    /**
     * Calcule le nombre moyen de jours de retard sur les paiements passés
     * Basé sur les échéances de RepaymentSchedule liées à l'utilisateur (via CreditRequest -> User)
     */
    private double calculateAverageDelayDays(Long userId) {
        List<RepaymentSchedule> paidSchedules = repaymentScheduleRepository
                .findPaidByUserId(userId, RepaymentStatus.PAID);

        if (paidSchedules == null || paidSchedules.isEmpty()) {
            // Aucun historique de paiement pour cet utilisateur
            return 0.0;
        }

        long totalDelayDays = 0L;
        int count = 0;

        for (RepaymentSchedule schedule : paidSchedules) {
            LocalDate dueDate = schedule.getDueDate();
            LocalDate paidDate = schedule.getPaidDate();

            if (dueDate == null || paidDate == null) {
                // Si la date d'échéance ou de paiement est manquante, on ignore cette échéance
                continue;
            }

            long delay = ChronoUnit.DAYS.between(dueDate, paidDate);

            // On ne considère que les retards (pas les paiements en avance)
            if (delay > 0) {
                totalDelayDays += delay;
            }

            count++;
        }

        if (count == 0) {
            return 0.0;
        }

        double avgDelay = (double) totalDelayDays / (double) count;
        logger.debug("avg_delay_days calculé pour userId={} : {} jours", userId, avgDelay);
        return avgDelay;
    }

    /**
     * Calcule l'instabilité des paiements (écart-type des retards en jours)
     */
    private double calculatePaymentInstability(Long userId) {
        List<RepaymentSchedule> paidSchedules = repaymentScheduleRepository
                .findPaidByUserId(userId, RepaymentStatus.PAID);

        if (paidSchedules == null || paidSchedules.size() < 2) {
            // Pas assez de données pour mesurer une instabilité
            return 0.0;
        }

        List<Double> delays = new ArrayList<>();

        for (RepaymentSchedule schedule : paidSchedules) {
            LocalDate dueDate = schedule.getDueDate();
            LocalDate paidDate = schedule.getPaidDate();

            if (dueDate == null || paidDate == null) {
                continue;
            }

            long delay = ChronoUnit.DAYS.between(dueDate, paidDate);
            if (delay > 0) {
                delays.add((double) delay);
            } else {
                delays.add(0.0);
            }
        }

        if (delays.size() < 2) {
            return 0.0;
        }

        // Moyenne des retards
        double sum = 0.0;
        for (Double d : delays) {
            sum += d;
        }
        double mean = sum / delays.size();

        // Variance
        double variance = 0.0;
        for (Double d : delays) {
            double diff = d - mean;
            variance += diff * diff;
        }
        variance /= delays.size();

        double instability = Math.sqrt(variance);
        logger.debug("payment_instability calculée pour userId={} : {}", userId, instability);
        return instability;
    }


    /**
     * Calcule le nombre de transactions mensuelles
     */
    private int calculateMonthlyTransactionCount(Long userId) {
        // TODO: Requête sur table Transaction pour ce userId du dernier mois
        // Pour l'instant, retourne une valeur simulée
        return (int) (Math.random() * 20) + 5; // Entre 5 et 25 transactions
    }

    /**
     * Calcule l'écart-type des montants de transactions
     */
    private double calculateTransactionAmountStd(Long userId) {
        // TODO: Calculer l'écart-type réel des montants de transactions
        // Pour l'instant, valeur simulée
        return 100.0 + (Math.random() * 200); // Entre 100 et 300
    }

    /**
     * Détecte les transactions dans des pays à haut risque
     */
    private int detectHighRiskCountryTransactions(Long userId) {
        // TODO: Vérifier les transactions avec pays à risque
        // Nécessite un champ "country" ou "location" dans Transaction
        return 0; // Par défaut: aucune transaction à risque
    }

    /**
     * Détecte les transactions nocturnes inhabituelles (22h-6h)
     */
    private int detectUnusualNightTransactions(Long userId) {
        // TODO: Vérifier les transactions entre 22h et 6h
        // Nécessite analyse des timestamps dans Transaction
        return 0; // Par défaut: aucune transaction nocturne inhabituelle
    }

    /**
     * Détecte un changement d'adresse récent (< 6 mois)
     */
    private int detectAddressChange(Long userId) {
        // TODO: Vérifier dans historique du profil si adresse a changé récemment
        // Nécessite table ProfileHistory ou champs created_at/updated_at
        return 0; // Par défaut: pas de changement
    }

    /**
     * Détecte un changement de téléphone récent
     */
    private int detectPhoneChange(Long userId) {
        // TODO: Vérifier historique téléphone
        return 0;
    }

    /**
     * Détecte un changement d'email récent
     */
    private int detectEmailChange(Long userId) {
        // TODO: Vérifier historique email dans User
        return 0;
    }

    /**
     * Détecte un changement de pays récent
     */
    private int detectCountryChange(Long userId) {
        // TODO: Vérifier historique pays
        return 0;
    }

    /**
     * Calcule le pourcentage de variation du revenu
     */
    private double calculateIncomeChangePercentage(Long userId) {
        // TODO: Comparer ancien revenu vs nouveau dans Profile
        // Nécessite historique des revenus ou champs income_old/income_new
        return 0.0; // Par défaut: pas de changement
    }

    /**
     * Détecte un changement d'emploi récent
     */
    private int detectEmploymentChange(Long userId) {
        // TODO: Vérifier historique emploi dans Profile
        return 0;
    }

    /**
     * Calcule le nombre de demandes de crédit faites par le client
     * dans les 6 derniers mois (comportement de multiplication des demandes = risque)
     */
    private int calculateRecentCreditRequests(Long userId) {
        // Calculer la date limite : 6 mois avant maintenant
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        // Compter les demandes de crédit depuis cette date
        long count = creditRequestRepository.countRecentCreditRequestsByUserId(userId, sixMonthsAgo);

        logger.debug("recent_credit_requests calculé pour userId={} : {} demandes en 6 mois", userId, count);
        return (int) count;
    }
}
