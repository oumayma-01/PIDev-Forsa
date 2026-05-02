package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ComplaintAiAssistant complaintAiAssistant;

    @Override
    public List<Feedback> retrieveAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Feedback retrieveFeedback(Long feedbackId) {
        if (feedbackId == null) return null;
        return feedbackRepository.findById(feedbackId).orElse(null);
    }

    @Override
    public List<Feedback> getFeedbacksByUsername(String username) {
        if (username == null || username.isBlank()) return Collections.emptyList();
        return feedbackRepository.findByUserUsername(username);
    }

    @Override
    public Feedback addFeedback(Feedback f) {
        if (f == null) return null;

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) f.setUser(user);

        return feedbackRepository.save(f);
    }

    @Override
    public void removeFeedback(Long feedbackId) {
        if (feedbackId == null) return;
        if (feedbackRepository.existsById(feedbackId)) {
            feedbackRepository.deleteById(feedbackId);
        }
    }

    @Override
    public Feedback modifyFeedback(Feedback feedback) {
        if (feedback == null || feedback.getId() == null) return null;
        Feedback existing = feedbackRepository.findById(feedback.getId()).orElse(null);
        if (existing == null) return null;
        feedback.setUser(existing.getUser());
        feedback.setCreatedAt(existing.getCreatedAt());
        return feedbackRepository.save(feedback);
    }

    @Override
    public Map<String, Object> getFeedbackSummaryReport() {
        long total = feedbackRepository.count();
        double avg = feedbackRepository.findAll().stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", total);
        res.put("avgRating", avg);
        return res;
    }

    @Override
    public List<Map<String, Object>> getFeedbackTrendsLastMonths(int months) {
        if (months <= 0) return Collections.emptyList();
        List<Date> dates = feedbackRepository.findAll().stream()
                .map(Feedback::getCreatedAt)
                .filter(Objects::nonNull)
                .toList();
        return trendsByMonth(dates, months);
    }

    @Override
    public List<Map<String, Object>> getAvgRatingByCategory() {
        List<Feedback> all = feedbackRepository.findAll().stream()
                .filter(f -> f.getRating() != null)
                .toList();

        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        grouped.put("VERY_SATISFIED", new ArrayList<>());
        grouped.put("SATISFIED", new ArrayList<>());
        grouped.put("NEUTRAL", new ArrayList<>());
        grouped.put("DISSATISFIED", new ArrayList<>());
        grouped.put("VERY_DISSATISFIED", new ArrayList<>());

        for (Feedback f : all) {
            String level;
            if (f.getSatisfactionLevel() != null && !f.getSatisfactionLevel().isBlank()) {
                level = f.getSatisfactionLevel();
            } else {
                int r = f.getRating();
                if (r >= 5) level = "VERY_SATISFIED";
                else if (r == 4) level = "SATISFIED";
                else if (r == 3) level = "NEUTRAL";
                else if (r == 2) level = "DISSATISFIED";
                else level = "VERY_DISSATISFIED";
            }
            grouped.getOrDefault(level, grouped.get("NEUTRAL")).add(f.getRating());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((key, ratings) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("group", key);
            double avg = ratings.isEmpty() ? 0.0 :
                    ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            row.put("avgRating", Math.round(avg * 10.0) / 10.0);
            result.add(row);
        });
        return result;
    }

    @Override
    public Feedback addFeedbackWithAI(Feedback f) {
        if (f == null) return null;

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) f.setUser(user);

        int rating = (f.getRating() == null) ? 3 : Math.max(1, Math.min(5, f.getRating()));
        f.setRating(rating);

        String ratingLevel = computeSatisfactionFromRating(rating);
        String sentiment = complaintAiAssistant.detectFeedbackSentiment(f.getComment());
        f.setSatisfactionLevel(resolveFinalSatisfaction(rating, ratingLevel, sentiment, f.getComment()));

        return feedbackRepository.save(f);
    }

    private String resolveFinalSatisfaction(int rating, String ratingLevel, String aiSentiment, String comment) {
        // Keep hard consistency for explicit ratings except neutral rating (3),
        // where text sentiment can reasonably disambiguate.
        if (rating != 3) return ratingLevel;

        String lexical = detectLexicalSentiment(comment);
        String sentiment = (lexical != null && !lexical.isBlank()) ? lexical : (aiSentiment == null ? "NEUTRAL" : aiSentiment);
        String normalized = sentiment.trim().toUpperCase(Locale.ROOT);
        if ("NEGATIVE".equals(normalized)) return "DISSATISFIED";
        if ("POSITIVE".equals(normalized)) return "SATISFIED";
        return "NEUTRAL";
    }

    private String detectLexicalSentiment(String comment) {
        if (comment == null || comment.isBlank()) return "NEUTRAL";
        String c = comment.toLowerCase(Locale.ROOT);
        String[] negatives = {"bad", "poor", "terrible", "awful", "horrible", "nul", "mauvais", "insatisf", "déçu", "decu"};
        for (String token : negatives) {
            if (c.contains(token)) return "NEGATIVE";
        }
        String[] positives = {"good", "great", "excellent", "perfect", "super", "bien", "satisf", "top"};
        for (String token : positives) {
            if (c.contains(token)) return "POSITIVE";
        }
        return "NEUTRAL";
    }

    private String computeSatisfactionFromRating(int rating) {
        if (rating >= 5) return "VERY_SATISFIED";
        if (rating == 4) return "SATISFIED";
        if (rating == 3) return "NEUTRAL";
        if (rating == 2) return "DISSATISFIED";
        return "VERY_DISSATISFIED";
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
        counts.forEach((k, v) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", k);
            row.put("count", v);
            res.add(row);
        });
        return res;
    }
}