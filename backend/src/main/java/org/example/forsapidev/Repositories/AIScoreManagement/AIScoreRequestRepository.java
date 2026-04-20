package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.AIScoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIScoreRequestRepository extends JpaRepository<AIScoreRequest, Long> {
    List<AIScoreRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);
}