package org.example.forsapidev.entities.CreditManagement;

/**
 * Type de ligne dans le tableau d'amortissement
 */
public enum LineType {
    /**
     * Ligne normale d'échéance
     */
    NORMAL,

    /**
     * Ligne de pénalité pour retard de paiement (200 DT)
     */
    PENALTY
}

