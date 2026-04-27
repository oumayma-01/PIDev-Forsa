package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {

    /**
     * Trouve le gift actif (non encore attribué) d'un client spécifique
     */
    Optional<Gift> findFirstByClientIdAndAwardedFalse(Long clientId);

    /**
     * Trouve le gift ayant une notification en attente pour un client
     */
    Optional<Gift> findFirstByClientIdAndNotificationPendingTrue(Long clientId);

    /**
     * Trouve le dernier gift d'un client (utile pour l'historique ou le backoffice)
     */
    Optional<Gift> findFirstByClientIdOrderByCreatedAtDesc(Long clientId);

    /**
     * Trouve tous les gifts non attribués qui ont atteint le seuil
     */
    List<Gift> findByAwardedFalseAndAccumulatedAmountGreaterThanEqual(java.math.BigDecimal threshold);

    /**
     * Trouve tous les gifts d'un client
     */
    List<Gift> findAllByClientId(Long clientId);

    /**
     * Vérifie l'existence d'un gift pour un client
     */
    boolean existsByClientId(Long clientId);
}

