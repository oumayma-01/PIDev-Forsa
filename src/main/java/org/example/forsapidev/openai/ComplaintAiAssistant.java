package org.example.forsapidev.openai;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Priority;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ComplaintAiAssistant {

    private static final Logger log = LoggerFactory.getLogger(ComplaintAiAssistant.class);
    private final OpenAiChatClient client;

    public ComplaintAiAssistant(OpenAiChatClient client) {
        this.client = client;
    }

    // =========================
    // Classification catégorie (IA)
    // =========================
    public String classifyCategory(String description) {
        if (description == null || description.isBlank()) return "AUTRE";

        String system = "Tu es un classificateur de réclamations. "
                + "Réponds UNIQUEMENT par une seule valeur parmi: "
                + "FINANCE, TECHNIQUE, FRAUDE, COMPTE, CREDIT, SUPPORT, AUTRE. "
                + "Aucun texte supplémentaire.";
        String user = "Description: " + description + "\nDonne la catégorie.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            for (Category c : Category.values()) {
                if (out.contains(c.name())) return c.name();
            }
        } catch (Exception e) {
            log.warn("classifyCategory IA failed, fallback keyword: {}", e.getMessage());
            // fallback keyword
            String desc = description.toLowerCase();
            if (desc.contains("paiement") || desc.contains("remboursement") || desc.contains("argent")) return "FINANCE";
            if (desc.contains("bug") || desc.contains("connexion") || desc.contains("application")) return "TECHNIQUE";
            if (desc.contains("fraude") || desc.contains("fraud")) return "FRAUDE";
            if (desc.contains("compte") || desc.contains("identifiant")) return "COMPTE";
            if (desc.contains("crédit") || desc.contains("credit")) return "CREDIT";
        }
        return "AUTRE";
    }

    // =========================
    // Génération réponse
    // =========================
    public String draftResponse(String category, String subject, String description) {
        if (description == null || description.isBlank()) return "Description manquante.";

        String system = "Tu es un agent support assurance. Réponds en français, 3 à 5 phrases, ton professionnel. "
                + "Ne promets pas de remboursement. Si une info manque, demande-la.";
        String user = "Catégorie: " + category + "\n"
                + "Sujet: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Rédige une réponse client.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("draftResponse failed: {}", e.getMessage());
            return "Nous avons bien reçu votre demande et reviendrons vers vous dans les plus brefs délais.";
        }
    }

    // =========================
    // Améliorer réponse
    // =========================
    public String improveResponse(String category, String subject, String description, String draftMessage) {
        if (draftMessage == null || draftMessage.isBlank()) {
            return draftResponse(category, subject, description);
        }

        String system = "Tu es un agent support assurance. Reformule et améliore le message. "
                + "Réponds en français, 3 à 5 phrases, ton professionnel. "
                + "Ne promets pas de remboursement. Si info manquante, pose une question.";
        String user = "Catégorie: " + category + "\n"
                + "Sujet: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Brouillon réponse:\n" + draftMessage + "\n"
                + "Réécris le message final.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("improveResponse failed: {}", e.getMessage());
            return draftMessage; // retourne le brouillon original si IA échoue
        }
    }

    // =========================
    // Analyse satisfaction
    // =========================
    public String analyzeFeedbackSatisfaction(Integer rating, String comment) {
        String system = "Tu es un classificateur. Réponds UNIQUEMENT par une seule valeur parmi: "
                + "VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED. "
                + "Aucun texte supplémentaire.";
        String user = "Feedback:\n"
                + "rating=" + rating + "\n"
                + "comment=" + (comment == null ? "" : comment) + "\n"
                + "Donne le niveau de satisfaction.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            // VERY_ en premier pour éviter le bug de substring
            if (out.contains("VERY_SATISFIED"))    return "VERY_SATISFIED";
            if (out.contains("VERY_DISSATISFIED")) return "VERY_DISSATISFIED";
            if (out.contains("SATISFIED"))         return "SATISFIED";
            if (out.contains("NEUTRAL"))           return "NEUTRAL";
            if (out.contains("DISSATISFIED"))      return "DISSATISFIED";
        } catch (Exception e) {
            log.warn("analyzeFeedbackSatisfaction IA failed, fallback rating: {}", e.getMessage());
        }

        // fallback sur le rating numérique
        if (rating == null)  return "NEUTRAL";
        if (rating <= 1)     return "VERY_DISSATISFIED";
        if (rating == 2)     return "DISSATISFIED";
        if (rating == 3)     return "NEUTRAL";
        if (rating == 4)     return "SATISFIED";
        return "VERY_SATISFIED"; // rating >= 5
    }

    // =========================
    // Insights à partir du rapport
    // =========================
    public String generateInsightsFromReport(String reportJsonLikeText) {
        if (reportJsonLikeText == null || reportJsonLikeText.isBlank())
            return "Aucune donnée disponible pour générer des insights.";

        String system = "Tu es un analyste qualité service client. "
                + "Résume en français en 5 à 8 phrases maximum. "
                + "Donne: (1) 3 constats, (2) 3 recommandations actionnables. "
                + "Ne mentionne pas 'IA' ni 'modèle'.";
        String user = "Voici des métriques internes (format texte/JSON):\n"
                + reportJsonLikeText
                + "\nGénère les insights.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("generateInsightsFromReport failed: {}", e.getMessage());
            return "Impossible de générer les insights pour le moment.";
        }
    }

    // =========================
    // Simuler priorité (IA + fallback)
    // =========================
    public Priority simulatePriority(String description) {
        if (description == null || description.isBlank()) return Priority.MEDIUM;

        String system = "Tu es un classificateur de priorité. "
                + "Réponds UNIQUEMENT par une valeur parmi: LOW, MEDIUM, HIGH, CRITICAL. "
                + "Aucun texte supplémentaire.";
        String user = "Description: " + description + "\nDonne la priorité.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            if (out.contains("CRITICAL")) return Priority.CRITICAL;
            if (out.contains("HIGH"))     return Priority.HIGH;
            if (out.contains("MEDIUM"))   return Priority.MEDIUM;
            if (out.contains("LOW"))      return Priority.LOW;
        } catch (Exception e) {
            log.warn("simulatePriority IA failed, fallback keyword: {}", e.getMessage());
        }

        // fallback keyword (CRITICAL en premier)
        String desc = description.toLowerCase();
        if (desc.contains("critique") || desc.contains("immédiat") || desc.contains("bloquant")) return Priority.CRITICAL;
        if (desc.contains("urgent")   || desc.contains("pressant"))                              return Priority.HIGH;
        if (desc.contains("mineur")   || desc.contains("faible") || desc.contains("insignifiant")) return Priority.LOW;
        return Priority.MEDIUM;
    }
}