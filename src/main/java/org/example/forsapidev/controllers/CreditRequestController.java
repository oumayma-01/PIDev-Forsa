package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.CreditRequestCreateDTO;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.Services.CreditRequestService;
import org.example.forsapidev.Services.amortization.AmortizationCalculatorService;
import org.example.forsapidev.Services.amortization.AmortizationResult;
import org.example.forsapidev.payload.response.AmortizationScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/credits")
public class CreditRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestController.class);

    private final CreditRequestService service;
    private final AmortizationCalculatorService amortizationService;
    private final UserRepository userRepository;

    public CreditRequestController(CreditRequestService service, AmortizationCalculatorService amortizationService, UserRepository userRepository) {
        this.service = service;
        this.amortizationService = amortizationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreditRequestCreateDTO dto) {
        try {
            // Récupérer l'utilisateur authentifié depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            logger.info("Création d'une demande de crédit pour l'utilisateur authentifié: {}", username);

            // Récupérer l'entité User complète depuis la base de données
            User authenticatedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Utilisateur authentifié non trouvé: " + username));

            // Créer la demande de crédit avec l'utilisateur authentifié
            CreditRequest created = service.createCreditRequest(dto, authenticatedUser);
            return ResponseEntity.ok(created);
        } catch (IllegalStateException e) {
            // configuration missing (TMM / Inflation) -> return 400 with helpful message
            logger.warn("Bad request while creating credit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // unexpected error -> log and return structured 500
            logger.error("Failed to create credit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "timestamp", Instant.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage()
                    )
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<CreditRequest>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditRequest> getById(@PathVariable Long id) {
        return ResponseEntity.of(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditRequest> update(@PathVariable Long id, @RequestBody CreditRequest update) {
        try {
            CreditRequest saved = service.updateCredit(id, update);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCredit(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/validate", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> validateCredit(@PathVariable Long id) {
        try {
            CreditRequest validated = service.validateCredit(id);
            return ResponseEntity.ok(validated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error validating credit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour l'agent : approuver une demande de crédit
     * POST /api/credits/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveCredit(@PathVariable Long id) {
        try {
            CreditRequest approved = service.approveCredit(id);
            logger.info("Crédit ID={} approuvé avec succès", id);
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            logger.warn("Crédit ID={} introuvable", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Crédit introuvable", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Tentative d'approbation d'un crédit dans un état invalide : {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "État invalide", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de l'approbation du crédit ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint pour l'agent : rejeter une demande de crédit
     * POST /api/credits/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectCredit(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Non spécifié") : "Non spécifié";

        try {
            CreditRequest rejected = service.rejectCredit(id, reason);
            logger.info("Crédit ID={} rejeté avec succès - Raison: {}", id, reason);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            logger.warn("Crédit ID={} introuvable", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Crédit introuvable", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Tentative de rejet d'un crédit dans un état invalide : {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "État invalide", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors du rejet du crédit ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint pour l'agent : lister les crédits en attente de revue (UNDER_REVIEW)
     * GET /api/credits/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<CreditRequest>> getPendingCredits() {
        List<CreditRequest> allCredits = service.getAll();
        List<CreditRequest> pending = allCredits.stream()
                .filter(c -> c.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.UNDER_REVIEW
                          || c.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.SUBMITTED)
                .collect(Collectors.toList());

        logger.info("Récupération de {} crédits en attente de revue", pending.size());
        return ResponseEntity.ok(pending);
    }

    /**
     * Endpoint pour simuler le tableau d'amortissement sans créer le crédit
     * GET /api/credits/simulate?principal=10000&rate=5.0&duration=12&type=ANNUITE_CONSTANTE
     */
    @GetMapping("/simulate")
    public ResponseEntity<?> simulateAmortization(
            @RequestParam BigDecimal principal,
            @RequestParam BigDecimal rate,
            @RequestParam Integer duration,
            @RequestParam(required = false, defaultValue = "AMORTISSEMENT_CONSTANT") String type) {
        try {
            var calculationType = org.example.forsapidev.entities.CreditManagement.AmortizationType.valueOf(type);
            AmortizationResult result = amortizationService.calculateSchedule(calculationType, principal, rate, duration);

            AmortizationScheduleResponse response = new AmortizationScheduleResponse();
            response.setCalculationType(calculationType);
            response.setPrincipal(principal);
            response.setAnnualRatePercent(rate);
            response.setDurationMonths(duration);
            response.setTotalInterest(result.getTotalInterest());
            response.setTotalAmount(result.getTotalAmount());
            response.setPeriods(result.getPeriods().stream()
                    .map(p -> new AmortizationScheduleResponse.PeriodDetail(
                            p.getMonthNumber(),
                            p.getPrincipalPayment(),
                            p.getInterestPayment(),
                            p.getTotalPayment(),
                            p.getRemainingBalance()
                    ))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error simulating amortization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour obtenir le tableau d'amortissement d'un crédit existant
     * GET /api/credits/{id}/schedule
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<?> getAmortizationSchedule(@PathVariable Long id) {
        try {
            CreditRequest credit = service.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Crédit non trouvé"));

            if (credit.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.SUBMITTED ||
                credit.getStatus() == org.example.forsapidev.entities.CreditManagement.CreditStatus.UNDER_REVIEW) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le crédit doit être validé pour voir le tableau d'amortissement"));
            }

            AmortizationResult result = amortizationService.calculateSchedule(
                    credit.getTypeCalcul(),
                    credit.getAmountRequested(),
                    BigDecimal.valueOf(credit.getInterestRate()),
                    credit.getDurationMonths()
            );

            AmortizationScheduleResponse response = new AmortizationScheduleResponse();
            response.setCreditId(credit.getId());
            response.setCalculationType(credit.getTypeCalcul());
            response.setPrincipal(credit.getAmountRequested());
            response.setAnnualRatePercent(BigDecimal.valueOf(credit.getInterestRate()));
            response.setDurationMonths(credit.getDurationMonths());
            response.setTotalInterest(result.getTotalInterest());
            response.setTotalAmount(result.getTotalAmount());
            response.setPeriods(result.getPeriods().stream()
                    .map(p -> new AmortizationScheduleResponse.PeriodDetail(
                            p.getMonthNumber(),
                            p.getPrincipalPayment(),
                            p.getInterestPayment(),
                            p.getTotalPayment(),
                            p.getRemainingBalance()
                    ))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting amortization schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
