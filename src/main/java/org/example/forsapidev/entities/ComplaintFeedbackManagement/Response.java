package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "response")
@Getter
@Setter
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ AJOUT : Validation du message
    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    @Column(columnDefinition = "TEXT")
    private String message;

    // ✅ AJOUT : Validation du rôle
    @NotBlank(message = "Le rôle du répondant est obligatoire")
    @Size(max = 50, message = "Le rôle ne doit pas dépasser 50 caractères")
    private String responderRole;

    // ✅ AJOUT : Validation du nom
    @NotBlank(message = "Le nom du répondant est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String responderName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date responseDate;

    // ✅ AJOUT : Validation du statut de réponse
    @Pattern(regexp = "PENDING|PROCESSED|SENT|FAILED",
            message = "Statut invalide. Valeurs acceptées : PENDING, PROCESSED, SENT, FAILED")
    private String responseStatus;

    @ManyToOne
    private Complaint complaint;

    // ✅ AJOUT : Méthode @PrePersist pour auto-remplir responseDate
    @PrePersist
    protected void onCreate() {
        responseDate = new Date();
        if (responseStatus == null || responseStatus.isEmpty()) {
            responseStatus = "PENDING";
        }
    }
}