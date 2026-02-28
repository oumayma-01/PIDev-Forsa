package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;

import java.util.List;
import java.util.Map;

public interface IFeedbackService {

    List<Feedback> retrieveAllFeedbacks();
    Feedback retrieveFeedback(Long feedbackId);
    Feedback addFeedback(Feedback f);
    void removeFeedback(Long feedbackId);
    Feedback modifyFeedback(Feedback feedback);

    // ✅ Reporting (sur feedbacks)
    Map<String, Object> getFeedbackSummaryReport();
    List<Map<String, Object>> getFeedbackTrendsLastMonths(int months);
    List<Map<String, Object>> getAvgRatingByCategory();
    Feedback addFeedbackWithAI(Feedback f);

}
