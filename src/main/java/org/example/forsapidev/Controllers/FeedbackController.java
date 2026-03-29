package org.example.forsapidev.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.example.forsapidev.Services.Interfaces.IFeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/retrieve-all-feedbacks")
    public ResponseEntity<List<Feedback>> getFeedbacks() {
        List<Feedback> feedbacks = feedbackService.retrieveAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-feedback/{feedback-id}")
    public ResponseEntity<Feedback> retrieveFeedback(
            @PathVariable("feedback-id") Long fId) {
        Feedback feedback = feedbackService.retrieveFeedback(fId);
        if (feedback == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(feedback);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add-feedback")
    public ResponseEntity<Feedback> addFeedback(@Valid @RequestBody Feedback f) {
        Feedback saved = feedbackService.addFeedback(f);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @DeleteMapping("/remove-feedback/{feedback-id}")
    public ResponseEntity<Void> removeFeedback(
            @PathVariable("feedback-id") Long fId) {
        feedbackService.removeFeedback(fId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/modify-feedback")
    public ResponseEntity<Feedback> modifyFeedback(@Valid @RequestBody Feedback f) {
        Feedback updated = feedbackService.modifyFeedback(f);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/report/summary")
    public ResponseEntity<Map<String, Object>> feedbackSummaryReport() {
        return ResponseEntity.ok(feedbackService.getFeedbackSummaryReport());
    }

    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/report/trends")
    public ResponseEntity<List<Map<String, Object>>> feedbackTrends(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(feedbackService.getFeedbackTrendsLastMonths(months));
    }

    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/report/avg-rating-by-category")
    public ResponseEntity<List<Map<String, Object>>> avgRatingByCategory() {
        return ResponseEntity.ok(feedbackService.getAvgRatingByCategory());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("timestamp", new Date());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Erreur de validation");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("messages", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add-feedback-ai")
    public ResponseEntity<Feedback> addFeedbackWithAI(@Valid @RequestBody Feedback f) {
        Feedback saved = feedbackService.addFeedbackWithAI(f);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
