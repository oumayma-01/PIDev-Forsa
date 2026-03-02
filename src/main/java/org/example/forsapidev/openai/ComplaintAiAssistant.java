package org.example.forsapidev.openai;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.PriorityLevel;
import org.example.forsapidev.openai.GrokChatClient;
import org.springframework.stereotype.Component;

@Component
public class ComplaintAiAssistant {

    private final GrokChatClient client;

    public ComplaintAiAssistant(GrokChatClient client) {
        this.client = client;
    }

    public String classifyCategory(String description) {
        String system = "Tu es un classificateur. Réponds uniquement par une seule valeur parmi: FINANCE, TECHNIQUE, SUPPORT_GENERAL.";
        String user = "Réclamation (description):\n" + (description == null ? "" : description);

        // Appel à Grok
        String out = client.chat(system, user).toUpperCase();

        // Logique de classification basée sur le retour de l'IA
        if (out.contains("FINANCE")) return "FINANCE";
        if (out.contains("TECHNIQUE")) return "TECHNIQUE";
        if (out.contains("SUPPORT_GENERAL")) return "SUPPORT_GENERAL";

        // Fallback manuel si l'IA ne répond pas correctement
        String desc = (description == null) ? "" : description.toLowerCase();
        if (desc.contains("paiement") || desc.contains("remboursement") || desc.contains("argent")) return "FINANCE";
        if (desc.contains("bug") || desc.contains("connexion") || desc.contains("application")) return "TECHNIQUE";

        return "SUPPORT_GENERAL";
    }

    public PriorityLevel classifyPriority(String description) {
        String system = "Tu es un classificateur de priorité de réclamation. Réponds uniquement par une valeur parmi : LOW, MEDIUM, HIGH, CRITICAL.";
        String user = "Réclamation (description) :\n" + (description == null ? "" : description);

        String out = client.chat(system, user).toUpperCase();

        if (out.contains("CRITICAL")) return PriorityLevel.CRITICAL;
        if (out.contains("HIGH")) return PriorityLevel.HIGH;
        if (out.contains("MEDIUM")) return PriorityLevel.MEDIUM;
        if (out.contains("LOW")) return PriorityLevel.LOW;

        // Fallback manuel basé sur des mots-clés critiques
        String desc = (description == null) ? "" : description.toLowerCase();
        if (desc.contains("fraude") || desc.contains("escroquerie") || desc.contains("vol")) return PriorityLevel.CRITICAL;
        if (desc.contains("argent") || desc.contains("blocage")) return PriorityLevel.HIGH;

        return PriorityLevel.LOW;
    }

    public String draftResponse(String category, String subject, String description) {
        String system = "Tu es un agent support assurance chez Forsa Insurance. Réponds en français (3-5 phrases) avec un ton professionnel. Ne promets pas de remboursement immédiat.";
        String user = String.format("Catégorie: %s\nSujet: %s\nDescription: %s\nRédige une réponse client.", category, subject, description);

        return client.chat(system, user);
    }

    public String analyzeFeedbackSatisfaction(Integer rating, String comment) {
        String system = "Tu es un analyste de satisfaction. Réponds uniquement par : VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED.";
        String user = "Rating: " + rating + "\nCommentaire: " + (comment == null ? "Aucun" : comment);

        String out = client.chat(system, user).toUpperCase();

        if (out.contains("VERY_SATISFIED")) return "VERY_SATISFIED";
        if (out.contains("SATISFIED")) return "SATISFIED";
        if (out.contains("NEUTRAL")) return "NEUTRAL";
        if (out.contains("VERY_DISSATISFIED")) return "VERY_DISSATISFIED";
        if (out.contains("DISSATISFIED")) return "DISSATISFIED";

        // Fallback basé sur la note numérique
        if (rating == null) return "NEUTRAL";
        if (rating <= 2) return "DISSATISFIED";
        if (rating >= 4) return "SATISFIED";
        return "NEUTRAL";
    }

    public String generateInsightsFromReport(String reportJsonLikeText) {
        String system = "Tu es un analyste qualité. Résume les métriques suivantes en français (5-8 phrases). Inclus 3 constats et 3 recommandations.";
        return client.chat(system, "Données du rapport :\n" + reportJsonLikeText);
    }

    public String improveResponse(String category, String subject, String description, String draftMessage) {
        String system = "Tu es un agent support expert. Reformule le brouillon suivant pour le rendre plus professionnel et empathique. Garde une réponse courte (3-5 phrases).";
        String user = String.format("Contexte: %s - %s\nDescription: %s\nBrouillon: %s", category, subject, description, draftMessage);

        return client.chat(system, user);
    }
}
