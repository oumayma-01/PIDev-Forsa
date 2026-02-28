package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ComplaintAiAssistant complaintAiAssistant;

    @Override
    public List<Feedback> retrieveAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Feedback retrieveFeedback(Long feedbackId) {
        return feedbackRepository.findById(feedbackId).orElse(null);
    }

    @Override
    public Feedback addFeedback(Feedback f) {
        return feedbackRepository.save(f);
    }

    @Override
    public void removeFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }

    @Override
    public Feedback modifyFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @Override
    public Feedback addFeedbackWithAI(Feedback f) {
        if (f.getSatisfactionLevel() == null || f.getSatisfactionLevel().isBlank()) {
            try {
                String level = complaintAiAssistant.analyzeFeedbackSatisfaction(f.getRating(), f.getComment());
                f.setSatisfactionLevel(level);
            } catch (Exception e) {
                Integer r = f.getRating();
                if (r == null) f.setSatisfactionLevel("NEUTRAL");
                else if (r <= 2) f.setSatisfactionLevel("DISSATISFIED");
                else if (r == 3) f.setSatisfactionLevel("NEUTRAL");
                else f.setSatisfactionLevel("SATISFIED");
            }
        }
        return feedbackRepository.save(f);
    }

    @Override
    public Map<String, Object> getFeedbackSummaryReport() {
        long count = feedbackRepository.count();
        Double avg = feedbackRepository.avgRating();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : feedbackRepository.countByRating()) {
            distribution.put((Integer) row[0], (Long) row[1]);
        }

        Map<String, Long> bySatisfaction = new LinkedHashMap<>();
        for (Object[] row : feedbackRepository.countBySatisfactionLevel()) {
            bySatisfaction.put(String.valueOf(row[0]), (Long) row[1]);
        }

        Map<Boolean, Long> byAnon = new HashMap<>();
        for (Object[] row : feedbackRepository.countByAnonymous()) {
            byAnon.put((Boolean) row[0], (Long) row[1]);
        }

        long anonCount = byAnon.getOrDefault(true, 0L);
        double anonymousRate = (count == 0) ? 0.0 : (anonCount * 1.0 / count);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("count", count);
        res.put("avgRating", avg);
        res.put("ratingDistribution", distribution);
        res.put("bySatisfactionLevel", bySatisfaction);
        res.put("anonymousRate", anonymousRate);
        return res;
    }

    @Override
    public List<Map<String, Object>> getFeedbackTrendsLastMonths(int months) {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        List<Date> dates = feedbacks.stream().map(Feedback::getCreatedAt).toList();
        return trendsByMonth(dates, months);
    }

    @Override
    public List<Map<String, Object>> getAvgRatingByCategory() {
        List<Feedback> feedbacks = feedbackRepository.findAll();

        Map<String, List<Integer>> map = new HashMap<>();
        for (Feedback f : feedbacks) {
            if (f.getComplaint() == null) continue;
            String category = f.getComplaint().getCategory();
            if (category == null) category = "UNKNOWN";
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(f.getRating());
        }

        List<Map<String, Object>> res = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> e : map.entrySet()) {
            double avg = e.getValue().stream().mapToInt(x -> x).average().orElse(0.0);
            res.add(new LinkedHashMap<>(Map.of(
                    "category", e.getKey(),
                    "count", (long) e.getValue().size(),
                    "avgRating", avg
            )));
        }

        res.sort(Comparator.comparing(o -> String.valueOf(o.get("category"))));
        return res;
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
            if (d == null) continue;
            String key = fmt.format(d);
            if (counts.containsKey(key)) counts.put(key, counts.get(key) + 1);
        }

        List<Map<String, Object>> res = new ArrayList<>();
        counts.forEach((k, v) -> res.add(new LinkedHashMap<>(Map.of("period", k, "count", v))));
        return res;
    }
}
