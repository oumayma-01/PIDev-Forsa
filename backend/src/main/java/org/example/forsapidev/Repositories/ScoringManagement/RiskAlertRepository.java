package org.example.forsapidev.Repositories.ScoringManagement;

import org.example.forsapidev.entities.ScoringManagement.AlertSeverity;
import org.example.forsapidev.entities.ScoringManagement.AlertType;
import org.example.forsapidev.entities.ScoringManagement.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    List<RiskAlert> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<RiskAlert> findByClientIdAndResolvedFalseOrderByCreatedAtDesc(Long clientId);

    List<RiskAlert> findByResolvedFalseOrderByCreatedAtDesc();

    List<RiskAlert> findBySeverityAndResolvedFalseOrderByCreatedAtDesc(AlertSeverity severity);

    @Query("SELECT a FROM RiskAlert a WHERE a.severity = 'CRITICAL' " +
            "AND a.resolved = false ORDER BY a.createdAt DESC")
    List<RiskAlert> findCriticalUnresolved();

    Long countByResolvedFalse();

    List<RiskAlert> findByNotificationSentFalseOrderByCreatedAtAsc();
}