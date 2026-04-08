package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.AgentRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.UserManagement.Agent;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Synchronisation "source of truth" = table user.
 *
 * Sans toucher l'entité User, on expose des méthodes à appeler SUR les opérations
 * de création/mise à jour/suppression d'un user pour refléter les changements sur `agent`.
 */
@Service
public class AgentRegistryService {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistryService.class);

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    public AgentRegistryService(AgentRepository agentRepository, UserRepository userRepository) {
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncAgentForUser(Long userId) {
        if (userId == null) return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() == null || user.getRole().getName() == null) return;

        boolean isAgent = user.getRole().getName() == ERole.AGENT;

        if (isAgent) {
            agentRepository.findByUserId(userId).ifPresentOrElse(existing -> {
                existing.setIsActive(Boolean.TRUE.equals(user.getIsActive()));
                if (existing.getFullName() == null || existing.getFullName().isBlank()) {
                    existing.setFullName(user.getUsername());
                }
                agentRepository.save(existing);
            }, () -> {
                Agent agent = new Agent();
                agent.setUser(user);
                agent.setFullName(user.getUsername() != null ? user.getUsername() : ("agent-" + userId));
                agent.setIsActive(Boolean.TRUE.equals(user.getIsActive()));
                agent.setIsBusy(false);
                agent.setCurrentAssignedRequestId(null);
                agentRepository.save(agent);
                log.info("✅ Agent créé automatiquement pour userId={} username={}", userId, user.getUsername());
            });
        } else {
            agentRepository.findByUserId(userId).ifPresent(agent -> {
                agentRepository.delete(agent);
                log.info("✅ Agent supprimé automatiquement car userId={} n'est plus AGENT", userId);
            });
        }
    }

    @Transactional
    public void deleteAgentForUser(Long userId) {
        if (userId == null) return;
        agentRepository.findByUserId(userId).ifPresent(agent -> {
            agentRepository.delete(agent);
            log.info("✅ Agent supprimé automatiquement pour userId={} (suppression user)", userId);
        });
    }
}

