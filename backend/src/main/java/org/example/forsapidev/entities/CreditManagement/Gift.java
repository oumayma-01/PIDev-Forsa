package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Gift - Gestion du système de cadeaux pour les clients
 * Accumulation de 1.5% du capital de chaque crédit
 * Attribution automatique quand >= 500 DT
 */
@Entity
@Table(name = "gift")
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID du client bénéficiaire
     */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /**
     * Montant accumulé (1.5% de chaque crédit)
     */
    @Column(name = "accumulated_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal accumulatedAmount = BigDecimal.ZERO;

    /**
     * Seuil déclenchant l'attribution du gift (par défaut 500 DT)
     */
    @Column(name = "threshold", precision = 18, scale = 2, nullable = false)
    private BigDecimal threshold = new BigDecimal("500.00");

    /**
     * Indique si le gift a été attribué
     */
    @Column(name = "awarded", nullable = false)
    private Boolean awarded = false;

    /**
     * Date d'attribution
     */
    @Column(name = "awarded_at")
    private LocalDateTime awardedAt;

    /**
     * Montant effectivement attribué
     */
    @Column(name = "awarded_amount", precision = 18, scale = 2)
    private BigDecimal awardedAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Gift() {
        this.createdAt = LocalDateTime.now();
        this.accumulatedAmount = BigDecimal.ZERO;
        this.threshold = new BigDecimal("500.00");
        this.awarded = false;
    }

    public Gift(Long clientId) {
        this();
        this.clientId = clientId;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAccumulatedAmount() {
        return accumulatedAmount;
    }

    public void setAccumulatedAmount(BigDecimal accumulatedAmount) {
        this.accumulatedAmount = accumulatedAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public Boolean getAwarded() {
        return awarded;
    }

    public void setAwarded(Boolean awarded) {
        this.awarded = awarded;
    }

    public LocalDateTime getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(LocalDateTime awardedAt) {
        this.awardedAt = awardedAt;
    }

    public BigDecimal getAwardedAmount() {
        return awardedAmount;
    }

    public void setAwardedAmount(BigDecimal awardedAmount) {
        this.awardedAmount = awardedAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Ajoute un montant à l'accumulation
     */
    public void addAccumulation(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.accumulatedAmount = this.accumulatedAmount.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Vérifie si le seuil est atteint
     */
    public boolean isThresholdReached() {
        return !awarded && accumulatedAmount.compareTo(threshold) >= 0;
    }

    /**
     * Marque le gift comme attribué
     */
    public void markAsAwarded(BigDecimal amount) {
        this.awarded = true;
        this.awardedAt = LocalDateTime.now();
        this.awardedAmount = amount;
        this.updatedAt = LocalDateTime.now();
    }
}

