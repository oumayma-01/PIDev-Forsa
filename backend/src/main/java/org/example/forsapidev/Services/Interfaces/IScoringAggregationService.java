package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ScoringManagement.ScoreResult;

public interface IScoringAggregationService {
    ScoreResult calculateClientScore(Long clientId);
    ScoreResult getLatestScore(Long clientId);
    ScoreResult getOrCalculateScore(Long clientId);
}