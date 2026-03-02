package org.example.forsapidev.DTO;

import jakarta.validation.constraints.*;

public class FeedbackCreateDTO {

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private Integer rating;

    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
    private String comment;

    @Pattern(
            regexp = "VERY_SATISFIED|SATISFIED|NEUTRAL|DISSATISFIED|VERY_DISSATISFIED",
            message = "Niveau de satisfaction invalide. Valeurs acceptées : VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED"
    )
    private String satisfactionLevel;

    private Boolean isAnonymous = false;

    @NotNull(message = "L'identifiant de la réclamation est obligatoire")
    private Long complaintId;
}
