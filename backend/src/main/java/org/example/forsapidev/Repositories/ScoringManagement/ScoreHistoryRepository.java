package org.example.forsapidev.Repositories.ScoringManagement;

import org.example.forsapidev.entities.ScoringManagement.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

    List<ScoreHistory> findByClientIdOrderByReRatingDateDesc(Long clientId);

    @Query("SELECT h FROM ScoreHistory h WHERE h.clientId = :clientId " +
            "ORDER BY h.reRatingDate DESC LIMIT :limit")
    List<ScoreHistory> findLastNChanges(@Param("clientId") Long clientId,
                                        @Param("limit") int limit);

    @Query("SELECT h FROM ScoreHistory h WHERE h.clientId = :clientId " +
            "AND ABS(h.scoreDelta) >= :minDelta " +
            "ORDER BY h.reRatingDate DESC")
    List<ScoreHistory> findSignificantChanges(@Param("clientId") Long clientId,
                                              @Param("minDelta") Double minDelta);
}