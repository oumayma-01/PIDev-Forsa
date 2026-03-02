package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.AgentRepository;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.UserManagement.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion de l'assignation des demandes de crédit aux agents
 * Règle : après génération du rapport IA, assigner automatiquement à un agent disponible
 */
@Service
public class AgentAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentAssignmentService.class);

    private final AgentRepository agentRepository;
    private final CreditRequestRepository creditRequestRepository;

    @Autowired
    public AgentAssignmentService(AgentRepository agentRepository,
                                  CreditRequestRepository creditRequestRepository) {
        this.agentRepository = agentRepository;
        this.creditRequestRepository = creditRequestRepository;
    }

    /**
     * Assigne une demande de crédit à un agent disponible
     * Utilise un verrou pessimiste pour éviter les conflits de concurrence
     */
    @Transactional
    public Agent assignCreditRequestToAgent(CreditRequest creditRequest) {
        if (creditRequest == null) {
            logger.error("Impossible d'assigner : credit request null");
            return null;
        }

        // Vérifier si déjà assigné
        if (creditRequest.getAgentId() != null) {
            logger.info("Demande de crédit {} déjà assignée à l'agent {}",
                    creditRequest.getId(), creditRequest.getAgentId());
            return agentRepository.findById(creditRequest.getAgentId()).orElse(null);
        }

        // Trouver un agent disponible avec verrou pessimiste
        Optional<Agent> availableAgentOpt = agentRepository.findFirstAvailableAgentWithLock();

        if (availableAgentOpt.isEmpty()) {
            logger.warn("Aucun agent disponible pour assigner la demande de crédit {}",
                    creditRequest.getId());
            return null; // Retourne null au lieu de lancer une exception
        }

        Agent agent = availableAgentOpt.get();

        // Assigner la demande à l'agent
        agent.assignRequest(creditRequest.getId());
        agentRepository.save(agent);

        // Mettre à jour le creditRequest
        creditRequest.setAgentId(agent.getId());
        creditRequestRepository.save(creditRequest);

        logger.info("✅ Demande de crédit {} assignée à l'agent {} ({})",
                creditRequest.getId(), agent.getId(), agent.getFullName());

        return agent;
    }

    /**
     * Libère un agent (marque comme disponible)
     */
    @Transactional
    public void releaseAgent(Long agentId) {
        Optional<Agent> agentOpt = agentRepository.findById(agentId);

        if (agentOpt.isEmpty()) {
            logger.error("Agent {} non trouvé", agentId);
            return;
        }

        Agent agent = agentOpt.get();
        Long previousRequestId = agent.getCurrentAssignedRequestId();

        agent.releaseRequest();
        agentRepository.save(agent);

        logger.info("✅ Agent {} libéré (demande précédente : {})", agentId, previousRequestId);

        // Après libération, essayer d'assigner des crédits en attente
        assignPendingCreditsToAvailableAgents();
    }

    /**
     * Libère l'agent assigné à une demande de crédit spécifique
     */
    @Transactional
    public void releaseAgentForCreditRequest(Long creditRequestId) {
        Optional<Agent> agentOpt = agentRepository.findByCurrentAssignedRequestId(creditRequestId);

        if (agentOpt.isEmpty()) {
            logger.warn("Aucun agent trouvé pour la demande {}", creditRequestId);
            return;
        }

        Agent agent = agentOpt.get();
        agent.releaseRequest();
        agentRepository.save(agent);

        logger.info("✅ Agent {} libéré pour la demande {}", agent.getId(), creditRequestId);
    }

    /**
     * Récupère tous les agents disponibles
     */
    public List<Agent> getAvailableAgents() {
        return agentRepository.findAvailableAgents();
    }

    /**
     * Récupère tous les agents actifs
     */
    public List<Agent> getActiveAgents() {
        return agentRepository.findByIsActiveTrue();
    }

    /**
     * Récupère tous les agents occupés
     */
    public List<Agent> getBusyAgents() {
        return agentRepository.findByIsBusyTrue();
    }

    /**
     * Récupère l'agent assigné à une demande
     */
    public Optional<Agent> getAgentForCreditRequest(Long creditRequestId) {
        return agentRepository.findByCurrentAssignedRequestId(creditRequestId);
    }

    /**
     * Crée un nouvel agent
     */
    @Transactional
    public Agent createAgent(Agent agent) {
        return agentRepository.save(agent);
    }

    /**
     * Active/désactive un agent
     */
    @Transactional
    public Agent toggleAgentActive(Long agentId, boolean active) {
        Optional<Agent> agentOpt = agentRepository.findById(agentId);

        if (agentOpt.isEmpty()) {
            logger.error("Agent {} non trouvé", agentId);
            return null;
        }

        Agent agent = agentOpt.get();
        agent.setIsActive(active);

        // Si on désactive un agent occupé, le libérer
        if (!active && agent.getIsBusy()) {
            agent.releaseRequest();
        }

        agentRepository.save(agent);

        logger.info("Agent {} {} ", agentId, active ? "activé" : "désactivé");

        return agent;
    }

    /**
     * Assigne les crédits en attente (SUBMITTED) aux agents disponibles
     * Appelé quand un agent se libère ou périodiquement
     */
    @Transactional
    public void assignPendingCreditsToAvailableAgents() {
        logger.info("Vérification des crédits en attente à assigner...");

        // Récupérer les crédits en SUBMITTED (non assignés)
        List<CreditRequest> pendingCredits = creditRequestRepository.findAll().stream()
                .filter(cr -> cr.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.SUBMITTED
                        && cr.getAgentId() == null)
                .toList();

        if (pendingCredits.isEmpty()) {
            logger.info("Aucun crédit en attente trouvé");
            return;
        }

        logger.info("Nombre de crédits en attente : {}", pendingCredits.size());

        // Pour chaque crédit en attente, essayer de l'assigner
        for (CreditRequest credit : pendingCredits) {
            Agent assignedAgent = assignCreditRequestToAgent(credit);
            if (assignedAgent != null) {
                // Si assigné, changer le statut en UNDER_REVIEW
                credit.setStatus(org.example.forsapidev.entities.CreditManagement.CreditStatus.UNDER_REVIEW);
                creditRequestRepository.save(credit);
                logger.info("Crédit {} assigné à l'agent {} et passé en UNDER_REVIEW",
                        credit.getId(), assignedAgent.getId());
            } else {
                // Aucun agent disponible, arrêter la boucle
                logger.info("Plus d'agents disponibles, arrêt de l'assignation");
                break;
            }
        }
    }
}
