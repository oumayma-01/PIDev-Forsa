package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CreditRequestRepository extends JpaRepository<CreditRequest, Long> {

    /**
     * Compte le nombre de demandes de crédit faites par un utilisateur
     * depuis une date donnée
     */
    @Query("SELECT COUNT(c) FROM CreditRequest c WHERE c.user.id = :userId AND c.requestDate >= :sinceDate")
    long countRecentCreditRequestsByUserId(@Param("userId") Long userId, @Param("sinceDate") LocalDateTime sinceDate);
}

