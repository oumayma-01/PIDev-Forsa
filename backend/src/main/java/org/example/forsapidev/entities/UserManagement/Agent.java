package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Agent - Représente un agent pouvant traiter des demandes de crédit
 * Un agent peut être occupé (busy) s'il traite actuellement une demande
 */
@Entity
@Table(name = "agent")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence vers l'utilisateur (User) qui est un agent
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Nom complet de l'agent
     */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Indique si l'agent est actuellement occupé
     */
    @Column(name = "is_busy", nullable = false)
    private Boolean isBusy = false;

    /**
     * ID de la demande de crédit actuellement assignée (si busy)
     */
    @Column(name = "current_assigned_request_id")
    private Long currentAssignedRequestId;

    /**
     * Indique si l'agent est actif (peut recevoir des assignations)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Agent() {
        this.createdAt = LocalDateTime.now();
        this.isBusy = false;
        this.isActive = true;
    }

    public Agent(User user, String fullName) {
        this();
        this.user = user;
        this.fullName = fullName;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsBusy() {
        return isBusy;
    }

    public void setIsBusy(Boolean isBusy) {
        this.isBusy = isBusy;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getCurrentAssignedRequestId() {
        return currentAssignedRequestId;
    }

    public void setCurrentAssignedRequestId(Long currentAssignedRequestId) {
        this.currentAssignedRequestId = currentAssignedRequestId;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
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
     * Assigne une demande à cet agent
     */
    public void assignRequest(Long creditRequestId) {
        this.currentAssignedRequestId = creditRequestId;
        this.isBusy = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Libère l'agent (fin de traitement de la demande)
     */
    public void releaseRequest() {
        this.currentAssignedRequestId = null;
        this.isBusy = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si l'agent est disponible pour une nouvelle assignation
     */
    public boolean isAvailable() {
        return isActive && !isBusy;
    }
}

