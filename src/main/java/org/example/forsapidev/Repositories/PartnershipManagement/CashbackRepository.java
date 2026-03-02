package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.Cashback;
import org.example.forsapidev.entities.PartnershipManagement.CashbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {

    List<Cashback> findByClientIdAndStatusOrderByEarnedAtDesc(Long clientId, CashbackStatus status);

    @Query("SELECT SUM(c.amount) FROM Cashback c WHERE c.clientId = :clientId " +
            "AND c.status = 'AVAILABLE'")
    Double getTotalAvailableCashback(@Param("clientId") Long clientId);

    @Query("SELECT c FROM Cashback c WHERE c.status = 'AVAILABLE' " +
            "AND c.expiresAt < CURRENT_TIMESTAMP")
    List<Cashback> findExpiredCashback();
}