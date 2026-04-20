package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.AIScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AIScoreRepository extends JpaRepository<AIScore, Long> {
    Optional<AIScore> findByClientId(Long clientId);
    boolean existsByClientId(Long clientId);
}