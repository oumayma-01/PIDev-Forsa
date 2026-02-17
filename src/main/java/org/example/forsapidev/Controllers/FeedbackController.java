package org.example.forsapidev.Controllers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Getter
@Setter
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @GetMapping("/retrieve-all-feedbacks")
    public List<Feedback> getFeedbacks() {
        return feedbackService.retrieveAllFeedbacks();
    }

    @GetMapping("/retrieve-feedback/{feedback-id}")
    public Feedback retrieveFeedback(@PathVariable("feedback-id") Long fId) {
        return feedbackService.retrieveFeedback(fId);
    }

    @PostMapping("/add-feedback")
    public Feedback addFeedback(@RequestBody Feedback f) {
        return feedbackService.addFeedback(f);
    }

    @DeleteMapping("/remove-feedback/{feedback-id}")
    public void removeFeedback(@PathVariable("feedback-id") Long fId) {
        feedbackService.removeFeedback(fId);
    }

    @PutMapping("/modify-feedback")
    public Feedback modifyFeedback(@RequestBody Feedback f) {
        return feedbackService.modifyFeedback(f);
    }
}
