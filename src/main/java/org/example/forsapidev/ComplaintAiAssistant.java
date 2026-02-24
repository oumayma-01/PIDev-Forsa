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

        // fallback simple (si le modèle répond mal)
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
}
