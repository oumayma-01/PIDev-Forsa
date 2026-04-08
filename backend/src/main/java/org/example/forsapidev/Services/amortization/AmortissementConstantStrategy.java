package org.example.forsapidev.Services.amortization;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pour le calcul en Amortissement Constant (mensuel)
 *
 * Formule: Amortissement mensuel = C / n
 *
 * À chaque mois:
 * - Intérêt = Capital restant × i
 * - Mensualité = Amortissement + Intérêt
 * - Nouveau capital restant = Ancien capital restant - Amortissement
 *
 * La mensualité diminue chaque mois car l'intérêt diminue
 */
@Component
public class AmortissementConstantStrategy implements AmortizationStrategy {

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

        // Amortissement constant par mois: C / n
        BigDecimal constantPrincipal = principal.divide(
                BigDecimal.valueOf(durationMonths),
                SCALE,
                ROUNDING
        );

        // Génération du tableau d'amortissement
        List<AmortizationResult.MonthlyPeriod> periods = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= durationMonths; month++) {
            // Calcul de l'intérêt du mois: Intérêt = Capital restant × i
            BigDecimal interestPayment = remainingPrincipal
                    .multiply(monthlyRate)
                    .setScale(SCALE, ROUNDING);

            // Principal amorti (constant, sauf dernier mois pour ajustement)
            BigDecimal principalPayment;
            if (month == durationMonths) {
                // Dernier mois: on prend exactement le capital restant
                principalPayment = remainingPrincipal;
            } else {
                principalPayment = constantPrincipal;
            }

            // Mensualité totale = Amortissement + Intérêt
            BigDecimal monthlyPayment = principalPayment.add(interestPayment)
                    .setScale(SCALE, ROUNDING);

            // Nouveau capital restant = Ancien capital - Amortissement
            BigDecimal newRemaining = remainingPrincipal.subtract(principalPayment);
            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                newRemaining = BigDecimal.ZERO;
            }

            periods.add(new AmortizationResult.MonthlyPeriod(
                    month,
                    principalPayment.setScale(SCALE, ROUNDING),
                    interestPayment,
                    monthlyPayment,
                    newRemaining.setScale(SCALE, ROUNDING)
            ));

            totalInterest = totalInterest.add(interestPayment);
            remainingPrincipal = newRemaining;
        }

        BigDecimal totalAmount = principal.add(totalInterest);
        return new AmortizationResult(periods, totalInterest.setScale(SCALE, ROUNDING), totalAmount.setScale(SCALE, ROUNDING));
    }
}

