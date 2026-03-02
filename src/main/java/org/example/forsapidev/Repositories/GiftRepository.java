package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {

    /**
     * Trouve le gift d'un client spécifique
     */
    Optional<Gift> findByClientId(Long clientId);

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

