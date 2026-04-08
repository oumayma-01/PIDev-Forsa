package org.example.forsapidev.entities.CreditManagement;

/**
 * Type de méthode de calcul du crédit (mensuelle)
 */
public enum AmortizationType {
    /**
     * Annuité constante - Mensualité fixe
     * Formule: A = C × i / (1 - (1 + i)^(-n))
     */
    ANNUITE_CONSTANTE,

    /**
     * Amortissement constant - Principal constant
     * Formule: Amortissement mensuel = C / n
     */
    AMORTISSEMENT_CONSTANT
}

