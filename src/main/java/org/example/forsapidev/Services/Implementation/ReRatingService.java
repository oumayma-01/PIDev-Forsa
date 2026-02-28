package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IReRatingService;
import org.example.forsapidev.Services.Interfaces.IRiskCalculationService;
import org.example.forsapidev.Services.Interfaces.IScoringAggregationService;
import org.example.forsapidev.entities.ScoringManagement.RiskAlert;
import org.example.forsapidev.entities.ScoringManagement.ScoreHistory;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.example.forsapidev.entities.ScoringManagement.AlertType;
import org.example.forsapidev.entities.ScoringManagement.AlertSeverity;
import org.example.forsapidev.Repositories.ScoringManagement.ScoreHistoryRepository;
import org.example.forsapidev.Repositories.ScoringManagement.ScoreResultRepository;
import org.example.forsapidev.Repositories.ScoringManagement.RiskAlertRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReRatingService implements IReRatingService {

    private final IScoringAggregationService scoringService;
    private final ScoreResultRepository scoreResultRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final RiskAlertRepository riskAlertRepository;

    @Override
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void reRateAllActiveClients() {
        log.info("Starting monthly re-rating for all active clients");

        List<Long> activeClientIds = List.of(1L, 2L, 3L, 5L, 8L);

        for (Long clientId : activeClientIds) {
            try {
                reRateClient(clientId, "SCHEDULED_MONTHLY");
            } catch (Exception e) {
                log.error("Failed to re-rate client: {}", clientId, e);
            }
        }

        log.info("Monthly re-rating completed");
    }

    @Override
    @Transactional
    public ScoreHistory reRateClient(Long clientId, String trigger) {
        log.info("Re-rating client: {} with trigger: {}", clientId, trigger);

        ScoreResult previousScore = scoreResultRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("No previous score for client: " + clientId));

        ScoreResult newScore = scoringService.calculateClientScore(clientId);

        double scoreDelta = newScore.getFinalScore() - previousScore.getFinalScore();
        boolean categoryChanged = !newScore.getRiskCategory().equals(previousScore.getRiskCategory());

        ScoreHistory history = ScoreHistory.builder()
                .clientId(clientId)
                .previousScoreId(previousScore.getId())
                .newScoreId(newScore.getId())
                .previousScore(previousScore.getFinalScore())
                .newScore(newScore.getFinalScore())
                .scoreDelta(scoreDelta)
                .previousCategory(previousScore.getRiskCategory())
                .newCategory(newScore.getRiskCategory())
                .categoryChanged(categoryChanged)
                .factor1Delta(newScore.getFactor1Score() - previousScore.getFactor1Score())
                .factor2Delta(newScore.getFactor2Score() - previousScore.getFactor2Score())
                .factor3Delta(newScore.getFactor3Score() - previousScore.getFactor3Score())
                .factor4Delta(newScore.getFactor4Score() - previousScore.getFactor4Score())
                .factor5Delta(newScore.getFactor5Score() - previousScore.getFactor5Score())
                .trigger(trigger)
                .reRatingDate(LocalDateTime.now())
                .build();

        ScoreHistory saved = scoreHistoryRepository.save(history);

        if (Math.abs(scoreDelta) >= 5) {
            createScoreChangeAlert(clientId, newScore, scoreDelta);
        }

        log.info("Re-rating completed - Client: {}, Delta: {}, Category changed: {}",
                clientId, scoreDelta, categoryChanged);

        return saved;
    }

    @Override
    public List<ScoreHistory> getClientReRatingHistory(Long clientId) {
        return scoreHistoryRepository.findByClientIdOrderByReRatingDateDesc(clientId);
    }

    private void createScoreChangeAlert(Long clientId, ScoreResult newScore, double scoreDelta) {
        AlertType alertType = scoreDelta > 0 ? AlertType.SCORE_DETERIORATION : AlertType.SCORE_DETERIORATION;
        AlertSeverity severity = Math.abs(scoreDelta) >= 10 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;

        String message = scoreDelta > 0
                ? String.format("Score amélioré de %.2f points", scoreDelta)
                : String.format("Score détérioré de %.2f points", Math.abs(scoreDelta));

        RiskAlert alert = RiskAlert.builder()
                .clientId(clientId)
                .scoreResultId(newScore.getId())
                .alertType(alertType)
                .severity(severity)
                .message(message)
                .recommendedAction(scoreDelta < 0 ? "Contacter le client pour comprendre la détérioration" : "Féliciter le client")
                .resolved(false)
                .notificationSent(false)
                .createdAt(LocalDateTime.now())
                .build();

        riskAlertRepository.save(alert);
    }
}