package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ScoringManagement.Recommendation;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;

import java.util.List;

public interface IRecommendationService {
    List<Recommendation> generateRecommendations(ScoreResult scoreResult);
    List<Recommendation> getActiveRecommendations(Long clientId);
    Recommendation markAsCompleted(Long recommendationId);
}