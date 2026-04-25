package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.CreditApplication;
import org.example.forsapidev.entities.AIScoreManagement.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {
    List<CreditApplication> findByClientId(Long clientId);
    Optional<CreditApplication> findByClientIdAndStatus(Long clientId, CreditStatus status);
    List<CreditApplication> findByStatus(CreditStatus status);
}