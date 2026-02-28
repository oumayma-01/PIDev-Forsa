package org.example.forsapidev.Repositories.ScoringManagement;

import org.example.forsapidev.entities.ScoringManagement.RiskCategory;
import org.example.forsapidev.entities.ScoringManagement.ScoreResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreResultRepository extends JpaRepository<ScoreResult, Long> {

    @Query("SELECT s FROM ScoreResult s WHERE s.clientId = :clientId " +
            "ORDER BY s.calculationDate DESC LIMIT 1")
    Optional<ScoreResult> findLatestByClientId(@Param("clientId") Long clientId);

    List<ScoreResult> findByClientIdOrderByCalculationDateDesc(Long clientId);

    @Query("SELECT s FROM ScoreResult s WHERE s.id IN " +
            "(SELECT MAX(s2.id) FROM ScoreResult s2 GROUP BY s2.clientId) " +
            "AND s.riskCategory = :category")
    List<ScoreResult> findLatestScoresByCategory(@Param("category") RiskCategory category);

    @Query("SELECT AVG(s.finalScore) FROM ScoreResult s " +
            "WHERE s.calculationDate >= :since")
    Double getAverageScore(@Param("since") LocalDateTime since);
}