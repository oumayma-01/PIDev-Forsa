package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.ClientVerifiedInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientVerifiedInfoRepository extends JpaRepository<ClientVerifiedInfo, Long> {
    Optional<ClientVerifiedInfo> findByClientId(Long clientId);
}