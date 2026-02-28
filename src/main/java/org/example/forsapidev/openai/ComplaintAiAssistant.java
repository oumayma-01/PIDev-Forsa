package org.example.forsapidev.openai;

import org.springframework.stereotype.Component;

@Component
public class ComplaintAiAssistant {

    private final OpenAiChatClient client;

    public ComplaintAiAssistant(OpenAiChatClient client) {
        this.client = client;
    }

    public String classifyCategory(String description) {
        String system = "Tu es un classificateur. Réponds uniquement par une seule valeur parmi: FINANCE, TECHNIQUE, SUPPORT_GENERAL.";
        String user = "Réclamation (description):\n" + (description == null ? "" : description);

        String out = client.chat(system, user).toUpperCase();

        if (out.contains("FINANCE")) return "FINANCE";
        if (out.contains("TECHNIQUE")) return "TECHNIQUE";
        if (out.contains("SUPPORT_GENERAL")) return "SUPPORT_GENERAL";

        String desc = description == null ? "" : description.toLowerCase();
        if (desc.contains("paiement") || desc.contains("remboursement") || desc.contains("argent")) return "FINANCE";
        if (desc.contains("bug") || desc.contains("connexion") || desc.contains("application")) return "TECHNIQUE";
        return "SUPPORT_GENERAL";
    }

    public String draftResponse(String category, String subject, String description) {
        String system = "Tu es un agent support assurance. Réponds en français, 3 à 5 phrases, ton professionnel. "
                + "Ne promets pas de remboursement. Si une info manque, demande-la.";
        String user = "Catégorie: " + category + "\n"
                + "Sujet: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Rédige une réponse client.";

        return client.chat(system, user);
    }

    public String analyzeFeedbackSatisfaction(Integer rating, String comment) {
        String system = "Tu es un classificateur. Réponds uniquement par une seule valeur parmi: "
                + "VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED.";

        String user = "Feedback:\n"
                + "rating=" + rating + "\n"
                + "comment=" + (comment == null ? "" : comment) + "\n"
                + "Donne le niveau de satisfaction.";

        String out = client.chat(system, user).toUpperCase();

        if (out.contains("VERY_SATISFIED")) return "VERY_SATISFIED";
        if (out.contains("SATISFIED")) return "SATISFIED";
        if (out.contains("NEUTRAL")) return "NEUTRAL";
        if (out.contains("VERY_DISSATISFIED")) return "VERY_DISSATISFIED";
        if (out.contains("DISSATISFIED")) return "DISSATISFIED";

        if (rating == null) return "NEUTRAL";
        if (rating <= 2) return "DISSATISFIED";
        if (rating == 3) return "NEUTRAL";
        return "SATISFIED";
    }

    public String generateInsightsFromReport(String reportJsonLikeText) {
        String system = "Tu es un analyste qualité service client. "
                + "Résume en français en 5 à 8 phrases maximum. "
                + "Donne: (1) 3 constats, (2) 3 recommandations actionnables. "
                + "Ne mentionne pas 'IA' ni 'modèle'.";

        String user = "Voici des métriques internes (format texte/JSON):\n"
                + reportJsonLikeText
                + "\nGénère les insights.";

        return client.chat(system, user);
    }

    public String improveResponse(String category, String subject, String description, String draftMessage) {
        String system = "Tu es un agent support assurance. Reformule et améliore le message. "
                + "Réponds en français, 3 à 5 phrases, ton professionnel. "
                + "Ne promets pas de remboursement. Si info manquante, pose une question.";

        String user = "Catégorie: " + category + "\n"
                + "Sujet: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Brouillon réponse:\n" + (draftMessage == null ? "" : draftMessage) + "\n"
                + "Réécris le message final.";

        return client.chat(system, user);
    }
}
