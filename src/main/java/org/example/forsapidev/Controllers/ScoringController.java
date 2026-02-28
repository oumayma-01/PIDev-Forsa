package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IAIExplainabilityService;
import org.example.forsapidev.Services.Interfaces.IRiskCalculationService;
import org.example.forsapidev.Services.Interfaces.IScoringAggregationService;
import org.example.forsapidev.entities.ScoringManagement.RiskMetrics;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final IScoringAggregationService aggregationService;
    private final IRiskCalculationService riskCalculationService;
    private final IAIExplainabilityService aiService;

    @PostMapping("/calculate/{clientId}")
    public ResponseEntity<ScoreResult> calculateScore(@PathVariable Long clientId) {
        ScoreResult result = aggregationService.calculateClientScore(clientId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/latest/{clientId}")
    public ResponseEntity<ScoreResult> getLatestScore(@PathVariable Long clientId) {
        ScoreResult result = aggregationService.getLatestScore(clientId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/risk/{clientId}")
    public ResponseEntity<RiskMetrics> calculateRisk(
            @PathVariable Long clientId,
            @RequestParam Double loanAmount,
            @RequestParam Integer durationMonths) {

        ScoreResult score = aggregationService.getOrCalculateScore(clientId);
        RiskMetrics metrics = riskCalculationService.calculateRiskMetrics(
                score, loanAmount, durationMonths
        );

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/explain/{clientId}")
    public ResponseEntity<String> explainScore(@PathVariable Long clientId) {
        ScoreResult score = aggregationService.getLatestScore(clientId);
        String explanation = aiService.generateScoreExplanation(score);
        return ResponseEntity.ok(explanation);
    }
}