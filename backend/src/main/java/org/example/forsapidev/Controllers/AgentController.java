package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Services.AgentAssignmentService;
import org.example.forsapidev.entities.UserManagement.Agent;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentAssignmentService agentAssignmentService;

    @Autowired
    public AgentController(AgentAssignmentService agentAssignmentService) {
        this.agentAssignmentService = agentAssignmentService;
    }

    /**
     * Récupère tous les agents disponibles
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/available")
    public ResponseEntity<List<Agent>> getAvailableAgents() {
        List<Agent> availableAgents = agentAssignmentService.getAvailableAgents();
        return ResponseEntity.ok(availableAgents);
    }

    /**
     * Récupère tous les agents actifs
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<Agent>> getActiveAgents() {
        List<Agent> activeAgents = agentAssignmentService.getActiveAgents();
        return ResponseEntity.ok(activeAgents);
    }

    /**
     * Récupère tous les agents occupés
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/busy")
    public ResponseEntity<List<Agent>> getBusyAgents() {
        List<Agent> busyAgents = agentAssignmentService.getBusyAgents();
        return ResponseEntity.ok(busyAgents);
    }

    /**
     * Récupère l'agent assigné à un crédit spécifique
     */
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @GetMapping("/credit/{creditId}")
    public ResponseEntity<?> getAgentForCredit(@PathVariable Long creditId) {
        Optional<Agent> agent = agentAssignmentService.getAgentForCreditRequest(creditId);
        if (agent.isPresent()) {
            return ResponseEntity.ok(agent.get());
        } else {
            return ResponseEntity.ok(Map.of("message", "Aucun agent assigné à ce crédit"));
        }
    }

    /**
     * Libère un agent spécifique (ADMIN seulement)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{agentId}/release")
    public ResponseEntity<?> releaseAgent(@PathVariable Long agentId) {
        try {
            agentAssignmentService.releaseAgent(agentId);
            return ResponseEntity.ok(Map.of("message", "Agent libéré avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Libère l'agent assigné à un crédit spécifique
     */
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PostMapping("/credit/{creditId}/release")
    public ResponseEntity<?> releaseAgentForCredit(@PathVariable Long creditId) {
        try {
            agentAssignmentService.releaseAgentForCreditRequest(creditId);
            return ResponseEntity.ok(Map.of("message", "Agent libéré pour le crédit " + creditId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
