package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Repositories.ComplaintNotificationRepository;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.Services.Interfaces.IComplaintNotificationService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Priority;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService implements IComplaintService {
    private static final double MICRO_CREDIT_MAX_AMOUNT = 5000.0;
    private static final double MICRO_CREDIT_MIN_AMOUNT = 100.0;

    private final ComplaintRepository complaintRepository;
    private final ComplaintAiAssistant complaintAiAssistant;
    private final UserRepository userRepository;
    private final ResponseRepository responseRepository;
    private final ComplaintNotificationRepository complaintNotificationRepository;
    private final FeedbackRepository feedbackRepository;
    private final IComplaintNotificationService notificationService;

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
        if (c.getCategory() == null) c.setCategory(Category.OTHER);
        return complaintRepository.save(c);
    }

    @Override
    public List<Complaint> getComplaintsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Collections.emptyList();
        }
        return complaintRepository.findByUserUsername(username);
    }

    @Override
    @Transactional
    public void removeComplaint(Long complaintId) {
        complaintNotificationRepository.deleteByComplaintId(complaintId);
        responseRepository.deleteByComplaintId(complaintId);
        complaintRepository.deleteById(complaintId);
    }

    @Override
    public Complaint modifyComplaint(Complaint complaint) {
        if (complaint == null || complaint.getId() == null) return null;
        Complaint existing = complaintRepository.findById(complaint.getId()).orElse(null);
        if (existing == null) return null;
        String oldStatus = existing.getStatus();

        existing.setSubject(complaint.getSubject());
        existing.setDescription(complaint.getDescription());
        existing.setCategory(complaint.getCategory());
        existing.setPriority(complaint.getPriority());
        if (complaint.getStatus() != null) {
            existing.setStatus(complaint.getStatus());
        }

        Complaint updated = complaintRepository.save(existing);
        if (complaint.getStatus() != null && !Objects.equals(oldStatus, complaint.getStatus())) {
            notificationService.notifyClientStatusChanged(updated.getId(), complaint.getStatus());
        }
        return updated;
    }

    @Override
    public Complaint addComplaintWithAI(Complaint c) {
        // 1. Category
        try {
            String cat = complaintAiAssistant.classifyCategory(c.getDescription());
            c.setCategory(Category.valueOf(cat.toUpperCase()));
        } catch (Exception e) {
            c.setCategory(Category.OTHER);
        }

        // 2. Priority
        try {
            c.setPriority(complaintAiAssistant.simulatePriority(c.getDescription()));
        } catch (Exception e) {
            c.setPriority(Priority.MEDIUM);
        }

        // 3. Other fields
        c.setSubject("AI Analysis: " + (c.getSubject() != null ? c.getSubject() : "New Ticket"));
        c.setStatus("OPEN");
        c.setCreatedAt(new Date());

        return complaintRepository.save(c);
    }

    @Override
    public Map<String, Long> getStatsByPriority() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getPriority() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getPriority().name(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, String> generateResponseForComplaint(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        if (c == null) return Map.of("error", "Complaint not found");

        String category = c.getCategory() != null ? c.getCategory().name() : "AUTRE";
        String subject = c.getSubject() != null ? c.getSubject() : "Your complaint";
        String description = c.getDescription() != null ? c.getDescription() : "";

        try {
            String responseText = complaintAiAssistant.draftResponse(category, subject, description);
            if (responseText == null || responseText.isBlank()) {
                responseText = buildFallbackResponse(category, subject, description);
            }
            return Map.of("response", responseText);
        } catch (Exception e) {
            return Map.of("response", buildFallbackResponse(category, subject, description));
        }
    }

    private String buildFallbackResponse(String category, String subject, String description) {
        return "Hello,\n\n"
                + "We have received your complaint regarding \"" + subject + "\" "
                + "in the category " + category + ".\n"
                + "Our team is analyzing your request: " + description + "\n"
                + "We will get back to you with more details as soon as possible.\n\n"
                + "Best regards,\nSupport Team.";
    }

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
            insights = "Insights unavailable (AI not configured).";
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
        notificationService.notifyClientResponseAdded(complaintId, r.getMessage());
        return saved;
    }

    @Override
    public void closeComplaintIfEligible(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        complaint.setStatus("CLOSED");
        complaintRepository.save(complaint);
        notificationService.notifyClientComplaintClosed(complaintId);
    }

    @Override
    public Map<String, Object> getFinancialImpactByComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        double amount = extractAmountFromDescription(complaint.getDescription());
        String amountSource = "DESCRIPTION";
        if (amount <= 0) {
            amount = simulatedAmountByPriority(complaint.getPriority());
            amountSource = "SIMULATED";
        }

        long daysSinceCreation = 0L;
        if (complaint.getCreatedAt() != null) {
            daysSinceCreation = Math.max(0L,
                    ChronoUnit.DAYS.between(complaint.getCreatedAt().toInstant(), Instant.now()));
        }

        double amountScore = amountScore(amount);
        double priorityScore = priorityScore(complaint.getPriority());
        double ageScore = Math.min(100.0, daysSinceCreation * 1.67);

        double impactScore = round2(
                (amountScore   * 0.50) +
                        (priorityScore * 0.35) +
                        (ageScore      * 0.15)
        );

        String riskLevel;
        if (impactScore >= 75) riskLevel = "CRITICAL";
        else if (impactScore >= 50) riskLevel = "HIGH";
        else if (impactScore >= 25) riskLevel = "MEDIUM";
        else riskLevel = "LOW";

        String creditCategory;
        if (amount <= 300) creditCategory = "MICRO_SMALL";
        else if (amount <= 1000) creditCategory = "MICRO_STANDARD";
        else if (amount <= 3000) creditCategory = "MICRO_MEDIUM";
        else creditCategory = "MICRO_LARGE";

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("complaintId", complaintId);
        res.put("complaintAmount", round2(amount));
        res.put("amountSource", amountSource);
        res.put("priority", complaint.getPriority() != null ? complaint.getPriority().name() : Priority.MEDIUM.name());
        res.put("daysSinceCreation", daysSinceCreation);
        res.put("financialImpactScore", impactScore);
        res.put("riskLevel", riskLevel);
        res.put("creditCategory", creditCategory);
        return res;
    }


    private double computeRequiredScore(double amount) {
        if (amount <= 0) return 40.0;
        else if (amount <= 300) return 35.0;
        else if (amount <= 500) return 40.0;
        else if (amount <= 1000) return 50.0;
        else if (amount <= 2000) return 60.0;
        else if (amount <= 3500) return 70.0;
        else if (amount <= 5000) return 80.0;
        else return 85.0;
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

    private double extractAmountFromDescription(String description) {
        if (description == null || description.isBlank()) {
            return -1.0;
        }

        Pattern pattern = Pattern.compile("(\\d+(?:[\\.,]\\d{1,2})?)");
        Matcher matcher = pattern.matcher(description);
        while (matcher.find()) {
            try {
                String raw = matcher.group(1).replace(",", ".");
                double parsed = Double.parseDouble(raw);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (Exception ignored) {
            }
        }
        return -1.0;
    }

    private double simulatedAmountByPriority(Priority priority) {
        if (priority == null) return 500.0;
        return switch (priority) {
            case LOW -> 200.0;
            case MEDIUM -> 500.0;
            case HIGH -> 1500.0;
            case CRITICAL -> 3000.0;
        };
    }

    private double priorityScore(Priority priority) {
        if (priority == null) return 50.0;
        return switch (priority) {
            case LOW -> 25.0;
            case MEDIUM -> 50.0;
            case HIGH -> 75.0;
            case CRITICAL -> 100.0;
        };
    }

    private double amountScore(double amount) {
        if (amount <= 0) {
            return 0.0;
        }
        double clampedAmount = Math.max(MICRO_CREDIT_MIN_AMOUNT, Math.min(amount, MICRO_CREDIT_MAX_AMOUNT));
        double normalized = (clampedAmount - MICRO_CREDIT_MIN_AMOUNT)
                / (MICRO_CREDIT_MAX_AMOUNT - MICRO_CREDIT_MIN_AMOUNT);
        return Math.max(0.0, Math.min(100.0, normalized * 100.0));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
