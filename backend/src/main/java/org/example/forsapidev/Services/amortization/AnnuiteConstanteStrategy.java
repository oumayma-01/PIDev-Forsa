package org.example.forsapidev.Services.amortization;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pour le calcul en Annuité Constante (mensuelle)
 *
 * Formule: A = C × i / (1 - (1 + i)^(-n))
 * Où:
 * - C = capital emprunté
 * - i = taux mensuel = T / 12 (T en décimal, pas en pourcentage)
 * - n = durée en mois
 * - A = mensualité constante
 *
 * À chaque mois:
 * - Intérêt = Capital restant × i
 * - Amortissement = A - Intérêt
 * - Nouveau capital restant = Ancien capital restant - Amortissement
 */
@Component
public class AnnuiteConstanteStrategy implements AmortizationStrategy {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final int PRECISION_SCALE = 10;

    @Override
    public AmortizationResult calculate(BigDecimal principal, BigDecimal annualRatePercent, int durationMonths) {
        // Validations
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le capital doit être positif");
        }
        if (annualRatePercent == null || annualRatePercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le taux doit être positif ou nul");
        }
        if (durationMonths <= 0) {
            throw new IllegalArgumentException("La durée doit être positive");
        }

        // Calcul du taux mensuel: i = T / 12
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(100), PRECISION_SCALE, ROUNDING)
                .divide(BigDecimal.valueOf(12), PRECISION_SCALE, ROUNDING);

        // Calcul de l'annuité constante: A = C × i / (1 - (1 + i)^(-n))
        BigDecimal monthlyPayment;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = principal.divide(BigDecimal.valueOf(durationMonths), SCALE, ROUNDING);
        } else {
            BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
            BigDecimal powerFactor = BigDecimal.ONE.divide(
                onePlusRate.pow(durationMonths),
                PRECISION_SCALE,
                ROUNDING
            );
            BigDecimal denominator = BigDecimal.ONE.subtract(powerFactor);
            monthlyPayment = principal
                .multiply(monthlyRate)
                .divide(denominator, SCALE, ROUNDING);
        }

        // Génération du tableau d'amortissement
        List<AmortizationResult.MonthlyPeriod> periods = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= durationMonths; month++) {
            BigDecimal interestPayment = remainingPrincipal
                    .multiply(monthlyRate)
                    .setScale(SCALE, ROUNDING);

            BigDecimal principalPayment;
            BigDecimal actualMonthlyPayment;

            if (month == durationMonths) {
                // Dernier mois: solde exact du capital restant
                principalPayment = remainingPrincipal;
                actualMonthlyPayment = principalPayment.add(interestPayment);
            } else {
                principalPayment = monthlyPayment.subtract(interestPayment);
                actualMonthlyPayment = monthlyPayment;
            }

            BigDecimal newRemaining = remainingPrincipal.subtract(principalPayment);
            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                newRemaining = BigDecimal.ZERO;
            }

            periods.add(new AmortizationResult.MonthlyPeriod(
                    month,
                    principalPayment.setScale(SCALE, ROUNDING),
                    interestPayment,
                    actualMonthlyPayment.setScale(SCALE, ROUNDING),
                    newRemaining.setScale(SCALE, ROUNDING)
            ));

            totalInterest = totalInterest.add(interestPayment);
            remainingPrincipal = newRemaining;
        }

        BigDecimal totalAmount = principal.add(totalInterest);
        return new AmortizationResult(periods, totalInterest.setScale(SCALE, ROUNDING), totalAmount.setScale(SCALE, ROUNDING));
    }
}

