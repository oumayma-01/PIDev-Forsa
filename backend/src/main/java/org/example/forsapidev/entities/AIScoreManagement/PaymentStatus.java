package org.example.forsapidev.entities.AIScoreManagement;

public enum PaymentStatus {
    PENDING,    // En attente
    PAID,       // Payé à temps
    LATE,       // En retard (1-7 jours)
    MISSED      // Manqué (>7 jours)
}