package org.example.forsapidev.Repositories.ScoringManagement;

import org.example.forsapidev.entities.ScoringManagement.RiskMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskMetricsRepository extends JpaRepository<RiskMetrics, Long> {

    @Query("SELECT r FROM RiskMetrics r WHERE r.clientId = :clientId " +
            "ORDER BY r.calculationDate DESC LIMIT 1")
    Optional<RiskMetrics> findLatestByClientId(@Param("clientId") Long clientId);

    Optional<RiskMetrics> findByLoanId(Long loanId);

    List<RiskMetrics> findByClientIdOrderByCalculationDateDesc(Long clientId);

    @Query("SELECT AVG(r.probabilityOfDefault) FROM RiskMetrics r")
    Double getAveragePD();

    @Query("SELECT SUM(r.expectedLoss) FROM RiskMetrics r WHERE r.loanId IS NOT NULL")
    Double getTotalExpectedLoss();
}