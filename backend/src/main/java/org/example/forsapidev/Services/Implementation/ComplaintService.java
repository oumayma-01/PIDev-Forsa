package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.DTO.ComplaintCreditEligibilityDTO;
import org.example.forsapidev.DTO.ComplaintFinancialImpactDTO;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Repositories.ResponseRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.Services.Interfaces.IScoringAggregationService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Priority;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;

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

    private final ComplaintRepository complaintRepository;
    private final ComplaintAiAssistant complaintAiAssistant;
    private final UserRepository userRepository;
    private final ResponseRepository responseRepository;
    private final FeedbackRepository feedbackRepository;
    private final IScoringAggregationService scoringAggregationService;

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
    public void removeComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
    }

    @Override
    public Complaint modifyComplaint(Complaint complaint) {
        if (complaint == null || complaint.getId() == null) return null;
        Complaint existing = complaintRepository.findById(complaint.getId()).orElse(null);
        if (existing == null) return null;

        existing.setSubject(complaint.getSubject());
        existing.setDescription(complaint.getDescription());
        existing.setCategory(complaint.getCategory());
        existing.setPriority(complaint.getPriority());
        if (complaint.getStatus() != null) {
            existing.setStatus(complaint.getStatus());
        }

        return complaintRepository.save(existing);
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
        return saved;
    }

    @Override
    public void closeComplaintIfEligible(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        complaint.setStatus("CLOSED");
        complaintRepository.save(complaint);
    }

    @Override
    public ComplaintCreditEligibilityDTO getCreditEligibilityByComplaint(Long complaintId, Double requiredScore) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        double required = (requiredScore == null || requiredScore <= 0) ? 70.0 : requiredScore;
        Long clientId = complaint.getUser() != null ? complaint.getUser().getId() : null;

        double currentScore;
        boolean fallbackUsed = false;

        if (clientId == null) {
            currentScore = 50.0;
            fallbackUsed = true;
        } else {
            try {
                ScoreResult scoreResult = scoringAggregationService.getOrCalculateScore(clientId);
                currentScore = scoreResult.getFinalScore() != null ? scoreResult.getFinalScore() : 50.0;
                if (scoreResult.getFinalScore() == null) {
                    fallbackUsed = true;
                }
            } catch (Exception e) {
                currentScore = 50.0;
                fallbackUsed = true;
            }
        }

        double gap = Math.max(0.0, required - currentScore);
        boolean eligible = currentScore >= required;

        return new ComplaintCreditEligibilityDTO(
                complaintId,
                clientId,
                round2(currentScore),
                round2(required),
                round2(gap),
                eligible,
                fallbackUsed
        );
    }

    @Override
    public ComplaintFinancialImpactDTO getFinancialImpactByComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        double amount = extractAmountFromDescription(complaint.getDescription());
        String amountSource = "SIMULATED";
        if (amount > 0) {
            amountSource = "DESCRIPTION";
        } else {
            amount = simulatedAmountByPriority(complaint.getPriority());
        }

        long daysSinceCreation = 0L;
        if (complaint.getCreatedAt() != null) {
            daysSinceCreation = Math.max(0L,
                    ChronoUnit.DAYS.between(complaint.getCreatedAt().toInstant(), Instant.now()));
        }

        double amountScore = amountScore(amount);
        double priorityScore = priorityScore(complaint.getPriority());
        double ageScore = Math.min(100.0, daysSinceCreation * 3.33);

        double impactScore = round2((amountScore * 0.50) + (priorityScore * 0.30) + (ageScore * 0.20));

        return new ComplaintFinancialImpactDTO(
                complaintId,
                round2(amount),
                amountSource,
                complaint.getPriority() != null ? complaint.getPriority().name() : Priority.MEDIUM.name(),
                daysSinceCreation,
                impactScore
        );
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
        if (priority == null) return 1000.0;
        return switch (priority) {
            case LOW -> 300.0;
            case MEDIUM -> 1000.0;
            case HIGH -> 2500.0;
            case CRITICAL -> 5000.0;
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
        double clampedAmount = Math.min(amount, MICRO_CREDIT_MAX_AMOUNT);
        double normalized = Math.log(clampedAmount + 1.0) / Math.log(MICRO_CREDIT_MAX_AMOUNT + 1.0);
        return Math.max(0.0, Math.min(100.0, normalized * 100.0));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
