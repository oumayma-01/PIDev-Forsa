package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;
import org.example.forsapidev.entities.PartnershipManagement.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PartnerTransactionRepository extends JpaRepository<PartnerTransaction, Long> {

    List<PartnerTransaction> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<PartnerTransaction> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    List<PartnerTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT t FROM PartnerTransaction t WHERE t.clientId = :clientId " +
            "AND t.createdAt >= :since")
    List<PartnerTransaction> findRecentByClient(@Param("clientId") Long clientId,
                                                @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM PartnerTransaction t WHERE t.clientId = :clientId " +
            "AND t.createdAt >= :since")
    Long countRecentTransactions(@Param("clientId") Long clientId,
                                 @Param("since") LocalDateTime since);

    @Query("SELECT t FROM PartnerTransaction t WHERE t.partnerId = :partnerId " +
            "AND t.createdAt BETWEEN :start AND :end")
    List<PartnerTransaction> findByPartnerAndDateRange(@Param("partnerId") Long partnerId,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.amount) FROM PartnerTransaction t " +
            "WHERE t.partnerId = :partnerId AND t.status = 'PAID'")
    Double getTotalVolumeByPartner(@Param("partnerId") Long partnerId);

    @Query("SELECT COUNT(DISTINCT t.clientId) FROM PartnerTransaction t " +
            "WHERE t.partnerId = :partnerId")
    Integer getUniqueCustomerCount(@Param("partnerId") Long partnerId);
}