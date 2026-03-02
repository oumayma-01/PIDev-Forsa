package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    @Column(columnDefinition = "TEXT")
    private String message;

    @NotBlank(message = "Le rôle du répondant est obligatoire")
    @Size(max = 50, message = "Le rôle ne doit pas dépasser 50 caractères")
    private String responderRole;

    @NotBlank(message = "Le nom du répondant est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String responderName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date responseDate;

    @Pattern(
            regexp = "PENDING|PROCESSED|SENT|FAILED",
            message = "Statut invalide. Valeurs acceptées : PENDING, PROCESSED, SENT, FAILED"
    )
    private String responseStatus;

    @JsonBackReference
    @ManyToOne
    private Complaint complaint;

    @PrePersist
    protected void onCreate() {
        responseDate = new Date();
        if (responseStatus == null || responseStatus.isEmpty()) responseStatus = "PENDING";
    }
}
