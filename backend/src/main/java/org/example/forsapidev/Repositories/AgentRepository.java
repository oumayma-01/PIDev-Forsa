package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.UserManagement.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * Trouve un agent par son user ID
     */
    Optional<Agent> findByUserId(Long userId);

    /**
     * Trouve tous les agents disponibles (actifs et non occupés)
     */
    @Query("SELECT a FROM Agent a WHERE a.isActive = true AND a.isBusy = false ORDER BY a.id")
    List<Agent> findAvailableAgents();

    /**
     * Trouve un agent disponible avec un verrou pessimiste (pour éviter la concurrence)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Agent a WHERE a.isActive = true AND a.isBusy = false ORDER BY a.id")
    List<Agent> findAvailableAgentsWithLock();

    /**
     * Trouve tous les agents actifs
     */
    List<Agent> findByIsActiveTrue();

    /**
     * Trouve tous les agents occupés
     */
    List<Agent> findByIsBusyTrue();

    /**
     * Trouve l'agent assigné à une demande spécifique
     */
    Optional<Agent> findByCurrentAssignedRequestId(Long requestId);

    /**
     * Trouve plusieurs agents par leur user ID, triés par ID croissant
     */
    List<Agent> findByUserIdOrderByIdAsc(Long userId);
}
