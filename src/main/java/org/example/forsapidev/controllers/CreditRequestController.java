package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.Services.CreditRequestService;
import org.example.forsapidev.Services.amortization.AmortizationCalculatorService;
import org.example.forsapidev.Services.amortization.AmortizationResult;
import org.example.forsapidev.payload.request.CreditRequestMultipart;
import org.example.forsapidev.payload.response.AmortizationScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Créer une demande de crédit avec rapport médical (multipart)
     * Endpoint unifié qui appelle l'API Python pour l'analyse complète
     */
    @Operation(summary = "Créer une demande de crédit avec rapport médical (multipart)", description = "Le user authentifié fait une demande de crédit et upload un PDF/IMAGE de rapport médical.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Multipart payload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CreditRequestMultipart.class)))
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @PostMapping(value = "/with-health-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWithHealthReport(@ModelAttribute CreditRequestMultipart dto) {

        try {
            // Récupérer l'utilisateur authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                logger.warn("Tentative de création de crédit sans authentification valide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_UNKNOWN");

            logger.info("Authenticated request by user='{}' role='{}'", username, role);

            logger.info("📋 Création d'une demande de crédit avec rapport médical pour l'utilisateur: {}", username);
            logger.info("   💰 Montant: {}, Durée: {} mois, Type: {}", dto.getAmountRequested(), dto.getDurationMonths(), dto.getTypeCalcul());

            MultipartFile healthReport = dto.getHealthReport();
            if (healthReport == null || healthReport.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le rapport médical est requis"));
            }

            String contentType = healthReport.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Le rapport médical doit être un fichier PDF ou une image")
                );
            }

            // Récupérer l'utilisateur
            User authenticatedUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Utilisateur authentifié non trouvé: " + username));

            // Log id et role de l'entité User pour vérification
            logger.info("Using User id={} role={}", authenticatedUser.getId(),
                    authenticatedUser.getRole() != null ? authenticatedUser.getRole().getName() : "NO_ROLE");

            // Créer la demande de crédit avec analyse unifiée (fraude + assurance)
            CreditRequest created = service.createCreditRequestWithHealthReport(
                    dto.getAmountRequested(),
                    dto.getDurationMonths(),
                    dto.getTypeCalcul(),
                    healthReport,
                    authenticatedUser
            );

            logger.info("✅ Demande de crédit créée avec succès - ID={}", created.getId());
            logger.info("   📊 Risque fraude: {} ({})", created.getRiskLevel(), created.getIsRisky() ? "RISKY" : "SAFE");
            logger.info("   🏥 Assurance: {}", created.getInsuranceIsReject() ? "REJETÉE" : "Taux " + created.getInsuranceRate() + "%");
            logger.info("   🎯 Décision globale: {}", created.getGlobalDecision());

            return ResponseEntity.ok(created);

        } catch (IllegalStateException e) {
            logger.warn("Bad request while creating credit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create credit with health report", e);
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CreditRequest>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CreditRequest> getById(@PathVariable Long id) {
        return ResponseEntity.of(service.getById(id));
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CreditRequest> update(@PathVariable Long id, @RequestBody CreditRequest update) {
        try {
            CreditRequest saved = service.updateCredit(id, update);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCredit(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
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

    @PreAuthorize("hasRole('AGENT')")
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
    @PreAuthorize("hasRole('AGENT')")
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
    @PreAuthorize("hasRole('AGENT')")
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
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
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
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
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
