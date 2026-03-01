package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ScoringManagement.ScoreHistory;

import java.util.List;

public interface IReRatingService {
    void reRateAllActiveClients();
    ScoreHistory reRateClient(Long clientId, String trigger);
    List<ScoreHistory> getClientReRatingHistory(Long clientId);
}