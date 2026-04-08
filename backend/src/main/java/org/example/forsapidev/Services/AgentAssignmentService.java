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

        if (creditRequest.getAgentId() != null) {
            logger.info("Demande de crédit {} déjà assignée à l'agent {}",
                    creditRequest.getId(), creditRequest.getAgentId());
            return agentRepository.findById(creditRequest.getAgentId()).orElse(null);
        }

        List<Agent> availableAgents = agentRepository.findAvailableAgentsWithLock();
        if (availableAgents == null || availableAgents.isEmpty()) {
            logger.warn("Aucun agent disponible pour assigner la demande de crédit {}", creditRequest.getId());
            return null;
        }

        Agent agent = availableAgents.get(0);
        agent.assignRequest(creditRequest.getId());
        agentRepository.save(agent);

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
     * Crée un nouvel agent en évitant les doublons pour un même user.
     */
    @Transactional
    public Agent createAgent(Agent agent) {
        if (agent == null || agent.getUser() == null || agent.getUser().getId() == null) {
            logger.warn("Création agent ignorée: agent/user invalide");
            return null;
        }

        List<Agent> existingAgents = agentRepository.findByUserIdOrderByIdAsc(agent.getUser().getId());
        if (existingAgents != null && !existingAgents.isEmpty()) {
            Agent canonical = existingAgents.get(0);
            canonical.setFullName(agent.getFullName() != null ? agent.getFullName() : canonical.getFullName());
            canonical.setIsActive(agent.getIsActive() != null ? agent.getIsActive() : canonical.getIsActive());
            canonical.setIsBusy(agent.getIsBusy() != null ? agent.getIsBusy() : canonical.getIsBusy());
            canonical.setCurrentAssignedRequestId(agent.getCurrentAssignedRequestId());

            if (existingAgents.size() > 1) {
                for (int i = 1; i < existingAgents.size(); i++) {
                    agentRepository.delete(existingAgents.get(i));
                }
                logger.warn("Doublons Agent nettoyés pour userId={} ({} lignes supprimées)",
                        agent.getUser().getId(), existingAgents.size() - 1);
            }

            return agentRepository.save(canonical);
        }

        return agentRepository.save(agent);
    }

    /**
     * Synchronise un agent existant pour un user donné en corrigeant les doublons éventuels.
     */
    @Transactional
    public Agent syncSingleAgentForUser(Agent incoming) {
        if (incoming == null || incoming.getUser() == null || incoming.getUser().getId() == null) {
            return null;
        }

        List<Agent> agents = agentRepository.findByUserIdOrderByIdAsc(incoming.getUser().getId());
        if (agents.isEmpty()) {
            return createAgent(incoming);
        }

        Agent canonical = agents.get(0);
        canonical.setFullName(incoming.getFullName() != null ? incoming.getFullName() : canonical.getFullName());
        canonical.setIsActive(incoming.getIsActive() != null ? incoming.getIsActive() : canonical.getIsActive());
        canonical.setIsBusy(incoming.getIsBusy() != null ? incoming.getIsBusy() : canonical.getIsBusy());
        if (incoming.getCurrentAssignedRequestId() != null) {
            canonical.setCurrentAssignedRequestId(incoming.getCurrentAssignedRequestId());
        }
        Agent saved = agentRepository.save(canonical);

        if (agents.size() > 1) {
            for (int i = 1; i < agents.size(); i++) {
                agentRepository.delete(agents.get(i));
            }
            logger.warn("Doublons Agent supprimés pour userId={} ({} doublons)", incoming.getUser().getId(), agents.size() - 1);
        }

        return saved;
    }

    /**
     * Vérifie et supprime tous les doublons d'agents pour un user.
     */
    @Transactional
    public void deduplicateAgentsForUser(Long userId) {
        if (userId == null) return;
        List<Agent> agents = agentRepository.findByUserIdOrderByIdAsc(userId);
        if (agents == null || agents.size() < 2) return;

        for (int i = 1; i < agents.size(); i++) {
            agentRepository.delete(agents.get(i));
        }
        logger.warn("Nettoyage doublons Agent exécuté pour userId={} : {} lignes supprimées", userId, agents.size() - 1);
    }

    /**
     * Assigne les crédits en attente (SUBMITTED) aux agents disponibles
     */
    @Transactional
    public void assignPendingCreditsToAvailableAgents() {
        logger.info("Vérification des crédits en attente à assigner...");

        List<CreditRequest> pendingCredits = creditRequestRepository.findAll().stream()
                .filter(cr -> cr.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.SUBMITTED
                        && cr.getAgentId() == null)
                .toList();

        if (pendingCredits.isEmpty()) {
            logger.info("Aucun crédit en attente trouvé");
            return;
        }

        logger.info("Nombre de crédits en attente : {}", pendingCredits.size());

        for (CreditRequest credit : pendingCredits) {
            Agent assignedAgent = assignCreditRequestToAgent(credit);
            if (assignedAgent != null) {
                credit.setStatus(org.example.forsapidev.entities.CreditManagement.CreditStatus.UNDER_REVIEW);
                creditRequestRepository.save(credit);
                logger.info("Crédit {} assigné à l'agent {} et passé en UNDER_REVIEW",
                        credit.getId(), assignedAgent.getId());
            } else {
                logger.info("Plus d'agents disponibles, arrêt de l'assignation");
                break;
            }
        }
    }
}
