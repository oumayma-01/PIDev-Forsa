package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.PriorityLevel;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService implements IComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintAiAssistant complaintAiAssistant;
    private final UserRepository userRepository;
    private final ResponseRepository responseRepository;
    private final FeedbackRepository feedbackRepository;

    // ─────────────────────────────────────────────
    // Mots-clés pour le calcul de priorité
    // ─────────────────────────────────────────────
    private static final List<String> HIGH_KEYWORDS = List.of(
            "urgent", "urgente", "critique", "bloqué", "bloquée",
            "panne", "impossible", "fraude", "disparu", "disparue",
            "volé", "volée", "inaccessible", "plante", "immédiat",
            "immédiatement", "plus du tout", "ne fonctionne plus",
            "depuis hier", "depuis ce matin", "emergency", "ne peut plus",
            "totalement", "complètement", "perdu", "grave"
    );

    private static final List<String> LOW_KEYWORDS = List.of(
            "question", "information", "documentation", "suggestion",
            "amélioration", "parfois", "occasionnellement", "mineur",
            "renseignement", "curiosité", "demande", "conseil", "avis"
    );

    // ─────────────────────────────────────────────
    // Calcul de priorité basé sur mots-clés + catégorie
    // ─────────────────────────────────────────────
    private PriorityLevel calculatePriority(String subject, String description, Category category) {
        String text = ((subject != null ? subject : "") + " " + (description != null ? description : "")).toLowerCase();

        // HIGH : mots-clés d'urgence détectés
        for (String kw : HIGH_KEYWORDS) {
            if (text.contains(kw)) return PriorityLevel.HIGH;
        }

        // FINANCE sans urgence explicite → toujours au moins MEDIUM
        if (category == Category.FINANCE) {
            return PriorityLevel.MEDIUM;
        }

        // LOW : mots-clés non urgents détectés
        for (String kw : LOW_KEYWORDS) {
            if (text.contains(kw)) return PriorityLevel.LOW;
        }

        // Défaut
        return PriorityLevel.MEDIUM;
    }

    // ─────────────────────────────────────────────
    // CRUD de base
    // ─────────────────────────────────────────────
    @Override
    public List<Complaint> retrieveAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint retrieveComplaint(Long complaintId) {
        return complaintRepository.findById(complaintId).orElse(null);
    }

    @Override
    public Complaint addComplaint(Complaint c) {
        c.setCreatedAt(new Date());

        if (c.getStatus() == null || c.getStatus().isEmpty()) {
            c.setStatus("OPEN");
        }
        if (c.getCategory() == null) {
            c.setCategory(Category.AUTRE);
        }

        // ✅ CORRIGÉ : priorité calculée selon mots-clés + catégorie
        if (c.getPriority() == null) {
            c.setPriority(calculatePriority(c.getSubject(), c.getDescription(), c.getCategory()));
        }

        return complaintRepository.save(c);
    }

    @Override
    public void removeComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
    }

    @Override
    public Complaint modifyComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    // ─────────────────────────────────────────────
    // Ajout avec IA
    // ─────────────────────────────────────────────
    @Override
    public Complaint addComplaintWithAI(Complaint c) {
        // 1) IA pour la catégorie
        String cat;
        try {
            cat = complaintAiAssistant.classifyCategory(c.getDescription());
        } catch (Exception e) {
            cat = "AUTRE";
        }

        if (cat == null) cat = "AUTRE";
        if ("SUPPORT_GENERAL".equalsIgnoreCase(cat)) cat = "SUPPORT";

        try {
            c.setCategory(Category.valueOf(cat.toUpperCase()));
        } catch (Exception e) {
            c.setCategory(Category.AUTRE);
        }

        // 2) Priorité : IA d'abord, fallback sur calculatePriority()
        if (c.getPriority() == null) {
            try {
                PriorityLevel aiPriority = complaintAiAssistant.classifyPriority(c.getDescription());
                c.setPriority(aiPriority != null ? aiPriority
                        : calculatePriority(c.getSubject(), c.getDescription(), c.getCategory()));
            } catch (Exception e) {
                // ✅ CORRIGÉ : fallback intelligent au lieu de MEDIUM par défaut
                c.setPriority(calculatePriority(c.getSubject(), c.getDescription(), c.getCategory()));
            }
        }

        // 3) Métadonnées communes
        c.setSubject("Analyse IA : " + (c.getSubject() != null ? c.getSubject() : "Nouveau ticket"));
        c.setStatus("OPEN");
        c.setCreatedAt(new Date());

        return complaintRepository.save(c);
    }

    // ─────────────────────────────────────────────
    // Génération de réponse IA
    // ─────────────────────────────────────────────
    @Override
    public Map<String, String> generateResponseForComplaint(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        if (c == null) {
            return Map.of("error", "Plainte non trouvée");
        }

        String category    = c.getCategory() != null ? c.getCategory().name() : "AUTRE";
        String subject     = c.getSubject() != null ? c.getSubject() : "Votre réclamation";
        String description = c.getDescription() != null ? c.getDescription() : "";

        try {
            String responseText = complaintAiAssistant.draftResponse(category, subject, description);
            if (responseText == null || responseText.isBlank()) {
                responseText = buildFallbackAiResponse(category, subject, description);
            }
            return Map.of("response", responseText);
        } catch (Exception e) {
            return Map.of("response", buildFallbackAiResponse(category, subject, description));
        }
    }

    private String buildFallbackAiResponse(String category, String subject, String description) {
        return "Bonjour,\n\n"
                + "Nous avons bien reçu votre réclamation concernant \"" + subject + "\" "
                + "dans la catégorie " + category + ".\n"
                + "Notre équipe est en train d'analyser votre demande : " + description + "\n"
                + "Nous reviendrons vers vous avec plus de détails dans les plus brefs délais.\n\n"
                + "Cordialement,\nService Support.";
    }

    // ─────────────────────────────────────────────
    // Rapports & statistiques
    // ─────────────────────────────────────────────
    @Override
    public Map<String, Object> generateFullReportWithAI() {
        Map<String, Object> base = getComplaintSummaryReport();
        String textForAi = "total=" + base.get("total")
                + "\nbyStatus=" + base.get("byStatus")
                + "\nbyCategory=" + base.get("byCategory");

        String insights;
        try {
            insights = complaintAiAssistant.generateInsightsFromReport(textForAi);
        } catch (Exception e) {
            insights = "Insights indisponibles (IA non configurée).";
        }

        Map<String, Object> res = new LinkedHashMap<>(base);
        res.put("insights", insights);
        return res;
    }

    @Override
    public Map<String, Object> getComplaintSummaryReport() {
        long total = complaintRepository.count();

        Map<String, Long> byStatus = complaintRepository.findAll().stream()
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));

        Map<String, Long> byCategory = complaintRepository.findAll().stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getCategory().name(), Collectors.counting()));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", total);
        res.put("byStatus", byStatus);
        res.put("byCategory", byCategory);
        return res;
    }

    @Override
    public List<Map<String, Object>> getComplaintTrendsLastMonths(int months) {
        List<Date> dates = complaintRepository.findAll().stream()
                .map(Complaint::getCreatedAt)
                .filter(Objects::nonNull)
                .toList();
        return trendsByMonth(dates, months);
    }

    @Override
    public Map<String, Long> getStatsByCategory() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getCategory().name(), Collectors.counting()));
    }

    @Override
    public Map<String, Long> getStatsByPriority() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getPriority() != null)
                .collect(Collectors.groupingBy(c -> c.getPriority().name(), Collectors.counting()));
    }

    // ─────────────────────────────────────────────
    // Affectation & gestion des réponses
    // ─────────────────────────────────────────────
    @Override
    public Complaint affectComplaintToUser(Long complaintId, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null) return null;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        complaint.setUser(user);
        return complaintRepository.save(complaint);
    }

    @Override
    public Response addResponseAndUpdateStatus(Long complaintId, Response r) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        r.setComplaint(complaint);
        Response saved = responseRepository.save(r);

        String st = complaint.getStatus() == null ? "OPEN" : complaint.getStatus();
        if ("OPEN".equals(st)) {
            complaint.setStatus("IN_PROGRESS");
        }
        if ("SENT".equals(saved.getResponseStatus())) {
            complaint.setStatus("RESOLVED");
        }

        complaintRepository.save(complaint);
        return saved;
    }

    @Override
    public void closeComplaintIfEligible(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        complaint.setStatus("CLOSED");
        complaintRepository.save(complaint);
    }

    // ─────────────────────────────────────────────
    // Utilitaire : tendances par mois
    // ─────────────────────────────────────────────
    private List<Map<String, Object>> trendsByMonth(List<Date> dates, int months) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            Calendar c2 = (Calendar) cal.clone();
            c2.add(Calendar.MONTH, -i);
            counts.put(fmt.format(c2.getTime()), 0L);
        }

        for (Date d : dates) {
            String key = fmt.format(d);
            if (counts.containsKey(key)) {
                counts.put(key, counts.get(key) + 1);
            }
        }

        List<Map<String, Object>> res = new ArrayList<>();
        counts.forEach((k, v) ->
                res.add(new LinkedHashMap<>(Map.of("period", k, "count", v)))
        );
        return res;
    }
}
