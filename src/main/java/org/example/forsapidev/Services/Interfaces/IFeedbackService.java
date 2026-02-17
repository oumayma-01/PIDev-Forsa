package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import java.util.List;

public interface IFeedbackService {

    List<Feedback> retrieveAllFeedbacks();
    Feedback retrieveFeedback(Long feedbackId);
    Feedback addFeedback(Feedback f);
    void removeFeedback(Long feedbackId);
    Feedback modifyFeedback(Feedback feedback);
}