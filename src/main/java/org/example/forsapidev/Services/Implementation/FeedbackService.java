package org.example.forsapidev.Services.Implementation;

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
        if (feedbackId == null) {
            return null;
        }
        return feedbackRepository.findById(feedbackId).orElse(null);
    }

    @Override
    public Feedback addFeedback(Feedback f) {
        if (f == null) {
            return null;
        }
        // Ici, addFeedback est “brut” (pas d’IA)
        return feedbackRepository.save(f);
    }

    @Override
    public void removeFeedback(Long feedbackId) {
        if (feedbackId == null) {
            return;
        }
        if (feedbackRepository.existsById(feedbackId)) {
            feedbackRepository.deleteById(feedbackId);
        }
        // Sinon tu peux éventuellement lever une exception custom
    }

    @Override
    public Feedback modifyFeedback(Feedback feedback) {
        if (feedback == null || feedback.getId() == null) {
            return null;
        }
        // Optionnel : vérifier que l’entité existe avant de sauver
        if (!feedbackRepository.existsById(feedback.getId())) {
            return null;
            // ou lancer une exception custom NotFound
        }
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
        if (months <= 0) {
            return Collections.emptyList();
        }

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
                        f -> (f.getSatisfactionLevel() == null || f.getSatisfactionLevel().isBlank())
                                ? "UNKNOWN"
                                : f.getSatisfactionLevel(),
                        Collectors.averagingInt(Feedback::getRating)
                ));

        List<Map<String, Object>> res = new ArrayList<>();
        avgBySat.forEach((k, v) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("group", k);
            row.put("avgRating", v);
            res.add(row);
        });
        return res;
    }

    /**
     * Version “avec IA simulée” :
     * - si satisfactionLevel est vide, on le déduit automatiquement à partir du rating
     */
    @Override
    public Feedback addFeedbackWithAI(Feedback f) {
        if (f == null) {
            return null;
        }

        if (f.getSatisfactionLevel() == null || f.getSatisfactionLevel().isBlank()) {
            int r = (f.getRating() == null) ? 3 : f.getRating();
            String level = computeSatisfactionFromRating(r);
            f.setSatisfactionLevel(level);
        }

        return feedbackRepository.save(f);
    }

    /**
     * Logique centralisée pour transformer un rating en satisfactionLevel.
     */
    private String computeSatisfactionFromRating(int rating) {
        if (rating >= 5) {
            return "VERY_SATISFIED";
        } else if (rating == 4) {
            return "SATISFIED";
        } else if (rating == 3) {
            return "NEUTRAL";
        } else if (rating == 2) {
            return "DISSATISFIED";
        } else {
            return "VERY_DISSATISFIED";
        }
    }

    private List<Map<String, Object>> trendsByMonth(List<Date> dates, int months) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Map<String, Long> counts = new LinkedHashMap<>();

        // Prépare les périodes vides sur les N derniers mois
        for (int i = months - 1; i >= 0; i--) {
            Calendar c2 = (Calendar) cal.clone();
            c2.add(Calendar.MONTH, -i);
            counts.put(fmt.format(c2.getTime()), 0L);
        }

        // Compte les feedbacks par mois
        for (Date d : dates) {
            String key = fmt.format(d);
            if (counts.containsKey(key)) {
                counts.put(key, counts.get(key) + 1);
            }
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
