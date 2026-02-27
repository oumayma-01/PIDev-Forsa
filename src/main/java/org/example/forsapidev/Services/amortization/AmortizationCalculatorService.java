package org.example.forsapidev.Services.amortization;

import org.example.forsapidev.entities.CreditManagement.AmortizationType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service orchestrateur pour les calculs d'amortissement mensuel.
 * Utilise le pattern Strategy pour sélectionner la méthode de calcul appropriée.
 */
@Service
public class AmortizationCalculatorService {

    private final Map<AmortizationType, AmortizationStrategy> strategies;

    public AmortizationCalculatorService(
            AnnuiteConstanteStrategy annuiteConstanteStrategy,
            AmortissementConstantStrategy amortissementConstantStrategy) {

        this.strategies = new HashMap<>();
        this.strategies.put(AmortizationType.ANNUITE_CONSTANTE, annuiteConstanteStrategy);
        this.strategies.put(AmortizationType.AMORTISSEMENT_CONSTANT, amortissementConstantStrategy);
    }

    /**
     * Calcule le tableau d'amortissement selon la méthode choisie
     *
     * @param type Type d'amortissement (ANNUITE_CONSTANTE ou AMORTISSEMENT_CONSTANT)
     * @param principal Capital emprunté
     * @param annualRatePercent Taux annuel en pourcentage
     * @param durationMonths Durée en mois
     * @return Résultat du calcul avec toutes les mensualités
     */
    public AmortizationResult calculateSchedule(
            AmortizationType type,
            BigDecimal principal,
            BigDecimal annualRatePercent,
            int durationMonths) {

        if (type == null) {
            throw new IllegalArgumentException("Le type d'amortissement est obligatoire");
        }

        AmortizationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("Aucune stratégie trouvée pour le type: " + type);
        }

        return strategy.calculate(principal, annualRatePercent, durationMonths);
    }

    /**
     * Calcule uniquement la première mensualité (pour aperçu rapide)
     */
    public BigDecimal calculateFirstMonthlyPayment(
            AmortizationType type,
            BigDecimal principal,
            BigDecimal annualRatePercent,
            int durationMonths) {

        AmortizationResult result = calculateSchedule(type, principal, annualRatePercent, durationMonths);

        if (!result.getPeriods().isEmpty()) {
            return result.getPeriods().get(0).getTotalPayment();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Calcule le coût total des intérêts
     */
    public BigDecimal calculateTotalInterest(
            AmortizationType type,
            BigDecimal principal,
            BigDecimal annualRatePercent,
            int durationMonths) {

        AmortizationResult result = calculateSchedule(type, principal, annualRatePercent, durationMonths);
        return result.getTotalInterest();
    }
}

