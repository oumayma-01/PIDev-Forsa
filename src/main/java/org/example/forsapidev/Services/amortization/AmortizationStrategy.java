package org.example.forsapidev.Services.amortization;

import java.math.BigDecimal;

/**
 * Interface Strategy pour les différentes méthodes de calcul d'amortissement mensuel
 */
public interface AmortizationStrategy {

    /**
     * Calcule le tableau d'amortissement mensuel
     *
     * @param principal Capital emprunté (C)
     * @param annualRatePercent Taux annuel en pourcentage (T)
     * @param durationMonths Durée en mois (n)
     * @return Résultat du calcul avec toutes les périodes mensuelles
     */
    AmortizationResult calculate(BigDecimal principal, BigDecimal annualRatePercent, int durationMonths);
}

