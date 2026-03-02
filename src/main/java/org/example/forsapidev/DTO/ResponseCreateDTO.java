package org.example.forsapidev.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResponseCreateDTO {

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    private String message;

    @NotBlank(message = "Le rôle du répondant est obligatoire")
    @Size(max = 50, message = "Le rôle ne doit pas dépasser 50 caractères")
    private String responderRole;

    @NotBlank(message = "Le nom du répondant est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String responderName;

    @NotNull(message = "L'identifiant de la réclamation est obligatoire")
    private Long complaintId;
}
