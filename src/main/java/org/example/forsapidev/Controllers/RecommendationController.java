package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IRecommendationService;
import org.example.forsapidev.Services.Interfaces.IScoringAggregationService;
import org.example.forsapidev.entities.ScoringManagement.Recommendation;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final IRecommendationService recommendationService;
    private final IScoringAggregationService scoringService;

    @PostMapping("/generate/{clientId}")
    public ResponseEntity<List<Recommendation>> generateRecommendations(@PathVariable Long clientId) {
        ScoreResult score = scoringService.getOrCalculateScore(clientId);
        List<Recommendation> recommendations = recommendationService.generateRecommendations(score);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Recommendation>> getClientRecommendations(@PathVariable Long clientId) {
        List<Recommendation> recommendations = recommendationService.getActiveRecommendations(clientId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Recommendation> markAsCompleted(@PathVariable Long id) {
        Recommendation updated = recommendationService.markAsCompleted(id);
        return ResponseEntity.ok(updated);
    }
}