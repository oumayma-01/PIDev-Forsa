package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.AIScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIScoreHistoryRepository extends JpaRepository<AIScoreHistory, Long> {
    List<AIScoreHistory> findByClientIdOrderByCreatedAtDesc(Long clientId);
}