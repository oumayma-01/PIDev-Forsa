package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
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
        if (c.getStatus() == null || c.getStatus().isEmpty()) c.setStatus("OPEN");
        if (c.getCategory() == null) c.setCategory(Category.AUTRE);
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

    @Override
    public Complaint addComplaintWithAI(Complaint c) {
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

        c.setSubject("Analyse IA : " + (c.getSubject() != null ? c.getSubject() : "Nouveau ticket"));
        c.setStatus("OPEN");
        c.setCreatedAt(new Date());
        return complaintRepository.save(c);
    }

    @Override
    public Map<String, String> generateResponseForComplaint(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        if (c == null) return Map.of("error", "Plainte non trouvée");

        try {
            String responseText = complaintAiAssistant.draftResponse(
                    c.getCategory() != null ? c.getCategory().name() : "AUTRE",
                    c.getSubject(),
                    c.getDescription()
            );
            return Map.of("response", responseText);
        } catch (Exception e) {
            return Map.of("response", "Réponse IA indisponible pour le moment.");
        }
    }

    @Override
    public Map<String, Object> generateFullReportWithAI() {
        Map<String, Object> base = getComplaintSummaryReport();
        String textForAi = "total=" + base.get("total") + "\nbyStatus=" + base.get("byStatus") + "\nbyCategory=" + base.get("byCategory");

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
        List<Complaint> complaints = complaintRepository.findAll();
        List<Date> dates = complaints.stream().map(Complaint::getCreatedAt).filter(Objects::nonNull).toList();
        return trendsByMonth(dates, months);
    }

    @Override
    public Map<String, Long> getStatsByCategory() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getCategory().name(), Collectors.counting()));
    }

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
        if ("OPEN".equals(st)) complaint.setStatus("IN_PROGRESS");
        if ("SENT".equals(saved.getResponseStatus())) complaint.setStatus("RESOLVED");

        complaintRepository.save(complaint);
        return saved;
    }

    @Override
    public void closeComplaintIfEligible(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        boolean hasFeedback = feedbackRepository.findByComplaintId(complaintId).isPresent();

        if (!"RESOLVED".equals(complaint.getStatus()))
            throw new IllegalStateException("Complaint must be RESOLVED before closing");
        if (!hasFeedback)
            throw new IllegalStateException("Feedback is required before closing");

        complaint.setStatus("CLOSED");
        complaintRepository.save(complaint);
    }

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
            if (counts.containsKey(key)) counts.put(key, counts.get(key) + 1);
        }

        List<Map<String, Object>> res = new ArrayList<>();
        counts.forEach((k, v) -> res.add(new LinkedHashMap<>(Map.of("period", k, "count", v))));
        return res;
    }
}

