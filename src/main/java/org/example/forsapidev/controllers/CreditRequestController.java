package org.example.forsapidev.controllers;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.services.CreditRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credits")
public class CreditRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CreditRequestController.class);

    private final CreditRequestService service;

    public CreditRequestController(CreditRequestService service) {
        this.service = service;
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
}
