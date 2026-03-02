package org.example.forsapidev.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.PriorityLevel;


public class ComplaintCreateDTO {

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(min = 5, max = 200, message = "Le sujet doit contenir entre 5 et 200 caractères")
    private String subject;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;   // ex: "FINANCE", "AUTRE", ...

    private Long userId;// optionnel si tu la déduis du token
    @NotNull(message = "La priorité est obligatoire")
    private PriorityLevel priority;

}
