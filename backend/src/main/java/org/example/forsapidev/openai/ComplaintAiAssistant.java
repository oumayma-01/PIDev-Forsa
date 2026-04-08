package org.example.forsapidev.openai;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Priority;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class ComplaintAiAssistant {

    private static final Logger log = LoggerFactory.getLogger(ComplaintAiAssistant.class);
    private final OpenAiChatClient client;

    public String classifyCategory(String description) {
        if (description == null || description.isBlank()) return "OTHER";

        String system = "You are a complaint classifier. "
                + "Reply ONLY with one value among: "
                + "FINANCE, TECHNICAL, FRAUD, ACCOUNT, CREDIT, SUPPORT, OTHER. "  // ← ANGLAIS
                + "No additional text.";
        String user = "Description: " + description + "\nGive the category.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            for (Category c : Category.values()) {
                if (out.contains(c.name())) return c.name();
            }
        } catch (Exception e) {
            log.warn("classifyCategory AI failed, fallback keyword: {}", e.getMessage());
            String desc = description.toLowerCase();
            if (desc.contains("payment") || desc.contains("refund") || desc.contains("money")
                    || desc.contains("paiement") || desc.contains("remboursement")) return "FINANCE";
            if (desc.contains("bug") || desc.contains("connection") || desc.contains("application")
                    || desc.contains("connexion")) return "TECHNICAL";  // ← TECHNIQUE → TECHNICAL
            if (desc.contains("fraud") || desc.contains("fraude")) return "FRAUD";  // ← FRAUDE → FRAUD
            if (desc.contains("account") || desc.contains("compte") || desc.contains("identifiant")) return "ACCOUNT";  // ← COMPTE → ACCOUNT
            if (desc.contains("credit") || desc.contains("crédit")) return "CREDIT";
        }
        return "OTHER";  // ← AUTRE → OTHER
    }

    public String draftResponse(String category, String subject, String description) {
        if (description == null || description.isBlank()) return "Description is missing.";

        String system = "You are an insurance support agent. Answer in English, 3 to 5 sentences, professional tone. "
                + "Do not promise refunds. If information is missing, ask for it.";
        String user = "Category: " + category + "\n"
                + "Subject: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Write a customer response.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("draftResponse failed: {}", e.getMessage());
            return "We have received your request and will get back to you as soon as possible.";
        }
    }

    public String improveResponse(String category, String subject, String description, String draftMessage) {
        if (draftMessage == null || draftMessage.isBlank()) {
            return draftResponse(category, subject, description);
        }

        String system = "You are an insurance support agent. Rewrite and improve the message. "
                + "Answer in English, 3 to 5 sentences, professional tone. "
                + "Do not promise refunds. If information is missing, ask a question.";
        String user = "Category: " + category + "\n"
                + "Subject: " + subject + "\n"
                + "Description: " + description + "\n"
                + "Draft response:\n" + draftMessage + "\n"
                + "Rewrite the final message.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("improveResponse failed: {}", e.getMessage());
            return draftMessage;
        }
    }

    public String analyzeFeedbackSatisfaction(Integer rating, String comment) {
        String system = "You are a classifier. Reply ONLY with one value among: "
                + "VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED. "
                + "No additional text.";
        String user = "Feedback:\n"
                + "rating=" + rating + "\n"
                + "comment=" + (comment == null ? "" : comment) + "\n"
                + "Give the satisfaction level.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            if (out.contains("VERY_SATISFIED"))    return "VERY_SATISFIED";
            if (out.contains("VERY_DISSATISFIED")) return "VERY_DISSATISFIED";
            if (out.contains("SATISFIED"))         return "SATISFIED";
            if (out.contains("NEUTRAL"))           return "NEUTRAL";
            if (out.contains("DISSATISFIED"))      return "DISSATISFIED";
        } catch (Exception e) {
            log.warn("analyzeFeedbackSatisfaction AI failed, fallback rating: {}", e.getMessage());
        }

        if (rating == null)  return "NEUTRAL";
        if (rating <= 1)     return "VERY_DISSATISFIED";
        if (rating == 2)     return "DISSATISFIED";
        if (rating == 3)     return "NEUTRAL";
        if (rating == 4)     return "SATISFIED";
        return "VERY_SATISFIED";
    }

    public String generateInsightsFromReport(String reportJsonLikeText) {
        if (reportJsonLikeText == null || reportJsonLikeText.isBlank())
            return "No data available to generate insights.";

        String system = "You are a customer service quality analyst. "
                + "Summarize in English in 5 to 8 sentences maximum. "
                + "Provide: (1) 3 findings, (2) 3 actionable recommendations. "
                + "Do not mention 'AI' or 'model'.";
        String user = "Here are internal metrics (text/JSON format):\n"
                + reportJsonLikeText
                + "\nGenerate the insights.";

        try {
            return client.chat(system, user);
        } catch (Exception e) {
            log.error("generateInsightsFromReport failed: {}", e.getMessage());
            return "Unable to generate insights at the moment.";
        }
    }

    public Priority simulatePriority(String description) {
        if (description == null || description.isBlank()) return Priority.MEDIUM;

        String system = "You are a priority classifier. "
                + "Reply ONLY with one value among: LOW, MEDIUM, HIGH, CRITICAL. "
                + "No additional text.";
        String user = "Description: " + description + "\nGive the priority.";

        try {
            String out = client.chat(system, user).toUpperCase().trim();
            if (out.contains("CRITICAL")) return Priority.CRITICAL;
            if (out.contains("HIGH"))     return Priority.HIGH;
            if (out.contains("MEDIUM"))   return Priority.MEDIUM;
            if (out.contains("LOW"))      return Priority.LOW;
        } catch (Exception e) {
            log.warn("simulatePriority AI failed, fallback keyword: {}", e.getMessage());
        }

        String desc = description.toLowerCase();
        if (desc.contains("critical") || desc.contains("immediate") || desc.contains("blocking")
                || desc.contains("critique") || desc.contains("immédiat") || desc.contains("bloquant")) return Priority.CRITICAL;
        if (desc.contains("urgent") || desc.contains("pressing") || desc.contains("pressant")) return Priority.HIGH;
        if (desc.contains("minor") || desc.contains("low") || desc.contains("mineur") || desc.contains("faible")) return Priority.LOW;
        return Priority.MEDIUM;
    }
}