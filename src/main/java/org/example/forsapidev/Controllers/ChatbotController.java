package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatbotController {

    private final ComplaintAiAssistant complaintAiAssistant;

    public ChatbotController(ComplaintAiAssistant complaintAiAssistant) {
        this.complaintAiAssistant = complaintAiAssistant;
    }

    // Chat général sur les réclamations
    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");

        // Ici on peut réutiliser OpenAI directement
        String system = "Tu es un assistant virtuel du service réclamations de Forsa Insurance. "
                + "Réponds en français, de manière courte et claire. "
                + "Tu aides l'utilisateur à comprendre comment créer, suivre ou fermer une réclamation.";
        String answer;
        try {
            answer = complaintAiAssistant.improveResponse(
                    "SUPPORT",
                    "Question générale",
                    message,
                    null
            );
        } catch (Exception e) {
            // Fallback si OpenAI n'est pas dispo
            answer = "Je suis un assistant virtuel. Pour créer une réclamation, utilisez le bouton "
                    + "\"Nouvelle réclamation\" puis décrivez votre problème. "
                    + "Vous pouvez ensuite suivre son statut (OPEN, IN_PROGRESS, RESOLVED, CLOSED).";
        }

        return Map.of("answer", answer);
    }
}
