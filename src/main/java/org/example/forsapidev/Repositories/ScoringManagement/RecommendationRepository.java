package org.example.forsapidev.Repositories.ScoringManagement;

import org.example.forsapidev.entities.ScoringManagement.Recommendation;
import org.example.forsapidev.entities.ScoringManagement.RecommendationPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByClientIdAndIsActiveTrueOrderByPriorityDesc(Long clientId);

    List<Recommendation> findByClientIdAndPriorityAndIsActiveTrue(
            Long clientId,
            RecommendationPriority priority
    );

    @Query("SELECT r FROM Recommendation r WHERE r.clientId = :clientId " +
            "AND r.isActive = true " +
            "ORDER BY r.estimatedScoreImpact DESC LIMIT :limit")
    List<Recommendation> findTopRecommendations(@Param("clientId") Long clientId,
                                                @Param("limit") int limit);

    List<Recommendation> findByClientIdAndIsFromAITrueAndIsActiveTrue(Long clientId);

    List<Recommendation> findByClientIdAndCompletedAtIsNotNull(Long clientId);
}