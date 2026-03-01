package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private Integer rating;

    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Pattern(
            regexp = "VERY_SATISFIED|SATISFIED|NEUTRAL|DISSATISFIED|VERY_DISSATISFIED",
            message = "Niveau de satisfaction invalide. Valeurs acceptées : VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED"
    )
    private String satisfactionLevel;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @NotNull(message = "Le champ 'isAnonymous' est obligatoire")
    private Boolean isAnonymous;

    @JsonIgnore
    @OneToOne
    private Complaint complaint;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (isAnonymous == null) isAnonymous = false;
    }
}
