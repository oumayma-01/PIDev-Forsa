package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IPartnerReviewService;
import org.example.forsapidev.entities.PartnershipManagement.PartnerReview;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partner-reviews")
@RequiredArgsConstructor
public class PartnerReviewController {

    private final IPartnerReviewService reviewService;

    @PostMapping
    public ResponseEntity<PartnerReview> createReview(
            @RequestParam Long partnerId,
            @RequestParam Long clientId,
            @RequestParam Long transactionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        PartnerReview review = reviewService.createReview(partnerId, clientId, transactionId, rating, comment);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerReview> updateReview(
            @PathVariable Long id,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment) {
        PartnerReview updated = reviewService.updateReview(id, rating, comment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerReview> getReview(@PathVariable Long id) {
        PartnerReview review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerReview>> getPartnerReviews(@PathVariable Long partnerId) {
        List<PartnerReview> reviews = reviewService.getPartnerReviews(partnerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/partner/{partnerId}/rating")
    public ResponseEntity<Double> getPartnerAverageRating(@PathVariable Long partnerId) {
        Double rating = reviewService.getPartnerAverageRating(partnerId);
        return ResponseEntity.ok(rating);
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportReview(@PathVariable Long id, @RequestParam String reason) {
        reviewService.reportReview(id, reason);
        return ResponseEntity.ok().build();
    }
}