package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ AJOUT : Validation du rating (note entre 1 et 5)
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private Integer rating;

    // ✅ AJOUT : Validation du commentaire (optionnel mais limité)
    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
    @Column(columnDefinition = "TEXT")
    private String comment;

    // ✅ AJOUT : Validation du niveau de satisfaction
    @Pattern(regexp = "VERY_SATISFIED|SATISFIED|NEUTRAL|DISSATISFIED|VERY_DISSATISFIED",
            message = "Niveau de satisfaction invalide. Valeurs acceptées : VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED")
    private String satisfactionLevel;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // ✅ AJOUT : Validation de isAnonymous
    @NotNull(message = "Le champ 'isAnonymous' est obligatoire")
    private Boolean isAnonymous;

    @OneToOne
    private Complaint complaint;

    // ✅ AJOUT : Méthode @PrePersist pour auto-remplir createdAt
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (isAnonymous == null) {
            isAnonymous = false;
        }
    }
}