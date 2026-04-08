package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.QRCodeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeSessionRepository extends JpaRepository<QRCodeSession, Long> {

    Optional<QRCodeSession> findBySessionId(String sessionId);

    List<QRCodeSession> findByPartnerIdOrderByGeneratedAtDesc(Long partnerId);

    @Query("SELECT q FROM QRCodeSession q WHERE q.sessionId = :sessionId " +
            "AND q.isUsed = false AND q.expiresAt > :now")
    Optional<QRCodeSession> findValidSession(@Param("sessionId") String sessionId,
                                             @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM QRCodeSession q WHERE q.expiresAt < :now AND q.isUsed = false")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(q) FROM QRCodeSession q WHERE q.expiresAt < :now AND q.isUsed = false")
    Long countExpiredSessions(@Param("now") LocalDateTime now);
}