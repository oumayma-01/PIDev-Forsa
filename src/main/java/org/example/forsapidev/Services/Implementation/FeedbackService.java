package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;

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
        List<Date> dates = feedbackRepository.findAll().stream()
                .map(Feedback::getCreatedAt)
                .filter(Objects::nonNull)
                .toList();

        return trendsByMonth(dates, months);
    }

    @Override
    public List<Map<String, Object>> getAvgRatingByCategory() {
        Map<String, Double> avgBySat = feedbackRepository.findAll().stream()
                .filter(f -> f.getRating() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getSatisfactionLevel() == null ? "UNKNOWN" : f.getSatisfactionLevel(),
                        Collectors.averagingInt(Feedback::getRating)
                ));

        List<Map<String, Object>> res = new ArrayList<>();
        avgBySat.forEach((k, v) -> res.add(new LinkedHashMap<>(Map.of("group", k, "avgRating", v))));
        return res;
    }

    @Override
    public Feedback addFeedbackWithAI(Feedback f) {
        if (f.getSatisfactionLevel() == null || f.getSatisfactionLevel().isBlank()) {
            int r = f.getRating() == null ? 3 : f.getRating();
            String level = (r >= 5) ? "VERY_SATISFIED"
                    : (r == 4) ? "SATISFIED"
                    : (r == 3) ? "NEUTRAL"
                    : (r == 2) ? "DISSATISFIED"
                    : "VERY_DISSATISFIED";
            f.setSatisfactionLevel(level);
        }
        return feedbackRepository.save(f);
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
