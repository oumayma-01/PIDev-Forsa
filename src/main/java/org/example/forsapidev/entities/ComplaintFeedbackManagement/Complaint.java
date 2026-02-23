package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.User;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "complaint")
@Getter
@Setter
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ AJOUT : Validation du subject
    @NotBlank(message = "Le sujet est obligatoire")
    @Size(min = 5, max = 200, message = "Le sujet doit contenir entre 5 et 200 caractères")
    private String subject;

    // ✅ AJOUT : Validation de la description
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    // ✅ AJOUT : Validation de la catégorie
    @Pattern(regexp = "TECHNIQUE|FINANCE|SUPPORT|FRAUDE|COMPTE|CREDIT|AUTRE",
            message = "Catégorie invalide. Valeurs acceptées : TECHNIQUE, FINANCE, SUPPORT, FRAUDE, COMPTE, CREDIT, AUTRE")
    private String category;

    // ✅ AJOUT : Validation du statut
    @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED|CLOSED|REJECTED",
            message = "Statut invalide. Valeurs acceptées : OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED")
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "complaint")
    private Set<Response> responses;

    @OneToOne(mappedBy = "complaint")
    private Feedback feedback;

    // ✅ AJOUT : Méthode @PrePersist pour auto-remplir createdAt et status
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (status == null || status.isEmpty()) {
            status = "OPEN";
        }
    }

    public void setId(Long id) {
        this.id = id;
    }
}