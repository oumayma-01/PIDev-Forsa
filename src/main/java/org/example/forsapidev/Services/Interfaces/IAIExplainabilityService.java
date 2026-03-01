package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ScoringManagement.Recommendation;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;

import java.util.List;

public interface IAIExplainabilityService {
    String generateScoreExplanation(ScoreResult scoreResult);
    List<Recommendation> generateImprovementRecommendations(ScoreResult scoreResult);
}