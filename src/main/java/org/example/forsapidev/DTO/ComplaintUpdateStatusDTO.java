package org.example.forsapidev.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.PriorityLevel;

public class ComplaintUpdateStatusDTO {

    @NotNull(message = "L'identifiant de la réclamation est obligatoire")
    private Long id;

    @Pattern(
            regexp = "OPEN|IN_PROGRESS|RESOLVED|CLOSED|REJECTED",
            message = "Statut invalide. Valeurs acceptées : OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED"
    )
    private String status;
    private PriorityLevel priority;
}
