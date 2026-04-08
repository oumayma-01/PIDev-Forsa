package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.TmmRateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Interest Rate Engine - Calcul simplifié du taux d'intérêt
 * Formule: TauxFinal = TMM + Marge Client + Prime Assurance
 */
@Service
public class InterestRateEngineService {

    private final TmmRateRepository tmmRepo;

    public InterestRateEngineService(TmmRateRepository tmmRepo) {
        this.tmmRepo = tmmRepo;
    }

    @Value("${engine.client.adder:2.0}")
    private BigDecimal clientAdderPercent; // +2% (marge client)

    @Value("${engine.insurance.premium:0.5}")
    private BigDecimal insurancePremiumPercent; // +0.5% (prime d'assurance)

    /**
     * Calcule le taux d'intérêt annuel final en pourcentage
     * Formule simplifiée: TMM + 2% (marge) + 0.5% (assurance)
     *
     * @param requestDate Date de la demande (utilisée pour trouver le TMM de l'année)
     * @param durationMonths Durée en mois (non utilisée dans le calcul, gardée pour compatibilité)
     * @param tmmOverride TMM manuel optionnel (si null, cherche en base)
     * @param inflationOverride Non utilisé (gardé pour compatibilité)
     * @return Taux annuel final en pourcentage
     */
    public BigDecimal computeAnnualRatePercent(LocalDateTime requestDate,
                                              int durationMonths,
                                              BigDecimal tmmOverride,
                                              BigDecimal inflationOverride) {
        Integer year = requestDate != null ? requestDate.getYear() : null;

        // Récupération du TMM
        BigDecimal tmm;
        if (tmmOverride != null) {
            tmm = tmmOverride;
        } else {
            if (year == null) {
                throw new IllegalStateException("La date de demande est requise pour déterminer le TMM");
            }
            tmm = tmmRepo.findByYear(year)
                    .map(r -> r.getPercent())
                    .orElseThrow(() -> new IllegalStateException(
                            "TMM pour l'année " + year + " non configuré. Veuillez l'ajouter via /api/admin/tmm"));
        }

        // Calcul du taux final simplifié: TMM + marges fixes
        BigDecimal finalRatePercent = tmm
                .add(clientAdderPercent)        // +2% marge client
                .add(insurancePremiumPercent);  // +0.5% prime d'assurance

        return finalRatePercent.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Convertit le taux annuel en montant d'intérêt mensuel
     * Formule: interestMonthly = principal × (taux annuel / 100) / 12
     */
    public BigDecimal computeMonthlyInterest(BigDecimal principal, BigDecimal annualRatePercent) {
        BigDecimal annualRate = annualRatePercent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        return principal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le principal fixe mensuel
     * Formule: principalMonthly = montant / durée en mois
     */
    public BigDecimal computeMonthlyPrincipal(BigDecimal amountRequested, int durationMonths) {
        return amountRequested.divide(BigDecimal.valueOf(durationMonths), 2, RoundingMode.HALF_UP);
    }
}
