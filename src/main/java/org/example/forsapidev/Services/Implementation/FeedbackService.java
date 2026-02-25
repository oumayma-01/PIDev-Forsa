package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.example.forsapidev.Repositories.FeedbackRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;

    public List<Feedback> retrieveAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Feedback retrieveFeedback(Long feedbackId) {
        return feedbackRepository.findById(feedbackId).orElse(null);
    }

    public Feedback addFeedback(Feedback f) {
        return feedbackRepository.save(f);
    }

    public void removeFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }

    public Feedback modifyFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
}