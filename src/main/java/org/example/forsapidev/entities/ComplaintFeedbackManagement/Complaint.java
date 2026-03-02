package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(min = 5, max = 200, message = "Le sujet doit contenir entre 5 et 200 caractères")
    private String subject;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Pattern(
            regexp = "OPEN|IN_PROGRESS|RESOLVED|CLOSED|REJECTED",
            message = "Statut invalide. Valeurs acceptées : OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED"
    )
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @NotNull(message = "La priorité est obligatoire")
    @Enumerated(EnumType.STRING)
    private PriorityLevel priority;

    @ManyToOne
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "complaint")
    private Set<Response> responses;

    @OneToOne(mappedBy = "complaint")
    private Feedback feedback;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();

        if (status == null || status.isEmpty()) {
            status = "OPEN";
        }

        if (category == null) {
            category = Category.AUTRE;
        }

        if (priority == null) {
            priority = PriorityLevel.MEDIUM;
        }
    }

    public void setId(Long id) {
        this.id = id;
    }
}
