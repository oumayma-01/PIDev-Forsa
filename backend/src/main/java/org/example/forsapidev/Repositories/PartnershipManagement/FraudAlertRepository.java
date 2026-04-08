package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.FraudAlert;
import org.example.forsapidev.entities.PartnershipManagement.FraudSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    List<FraudAlert> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<FraudAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<FraudAlert> findBySeverityAndIsResolvedFalseOrderByCreatedAtDesc(FraudSeverity severity);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.clientId = :clientId " +
            "AND f.isResolved = false")
    Long countUnresolvedByClient(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.isResolved = false " +
            "AND f.severity = 'CRITICAL'")
    Long countCriticalUnresolved();
}