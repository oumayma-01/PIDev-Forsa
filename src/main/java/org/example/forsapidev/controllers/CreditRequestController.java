package org.example.forsapidev.Controllers;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.Services.CreditRequestService;
import org.example.forsapidev.Services.amortization.AmortizationCalculatorService;
import org.example.forsapidev.Services.amortization.AmortizationResult;
import org.example.forsapidev.payload.response.AmortizationScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/credits")
public class CreditRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestController.class);

    private final CreditRequestService service;
    private final AmortizationCalculatorService amortizationService;

    public CreditRequestController(CreditRequestService service, AmortizationCalculatorService amortizationService) {
        this.service = service;
        this.amortizationService = amortizationService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreditRequest request) {
        try {
            CreditRequest created = service.createCreditRequest(request);
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
