package org.example.forsapidev.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final IComplaintService complaintService;

    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.retrieveAllComplaints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaint(@PathVariable Long id) {
        Complaint complaint = complaintService.retrieveComplaint(id);
        if (complaint == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        }
        return ResponseEntity.ok(complaint);
    }

    // ✅ Ajout de @Valid
    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody Complaint c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addComplaint(c));
    }

    // ✅ Ajout de @Valid
    @PutMapping("/{id}")
    public ResponseEntity<Complaint> updateComplaint(@PathVariable Long id, @Valid @RequestBody Complaint c) {
        Complaint existing = complaintService.retrieveComplaint(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        }
        c.setId(id);
        return ResponseEntity.ok(complaintService.modifyComplaint(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        Complaint existing = complaintService.retrieveComplaint(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        }
        complaintService.removeComplaint(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign/{userId}")
    public ResponseEntity<Complaint> assignComplaintToUser(@PathVariable Long id, @PathVariable Long userId) {
        Complaint complaint = complaintService.affectComplaintToUser(id, userId);
        if (complaint == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint or User not found");
        }
        return ResponseEntity.ok(complaint);
    }

    @PostMapping("/{id}/responses")
    public ResponseEntity<Response> addResponse(@PathVariable Long id, @Valid @RequestBody Response r) {
        try {
            Response savedResponse = complaintService.addResponseAndUpdateStatus(id, r);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResponse);
        } catch (IllegalArgumentException e) {
            // Transforme l'IllegalArgumentException du service en 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Map<String, String>> closeComplaint(@PathVariable Long id) {
        try {
            complaintService.closeComplaintIfEligible(id);
            return ResponseEntity.ok(Map.of("status", "Complaint closed successfully"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            // Par exemple: plainte pas résolue ou pas de feedback
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryReport() {
        return ResponseEntity.ok(complaintService.getComplaintSummaryReport());
    }

    @GetMapping("/summary/ai")
    public ResponseEntity<Map<String, Object>> getSummaryReportWithAI() {
        return ResponseEntity.ok(complaintService.generateFullReportWithAI());
    }

    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getTrends(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(complaintService.getComplaintTrendsLastMonths(months));
    }

    @GetMapping("/stats-by-category")
    public ResponseEntity<Map<String, Long>> getStatsByCategory() { // Adapté si la map du service renvoie des String
        return ResponseEntity.ok(complaintService.getStatsByCategory());
    }

    @PostMapping("/{id}/ai-response")
    public ResponseEntity<Map<String, String>> generateAIResponse(@PathVariable Long id) {
        Map<String, String> response = complaintService.generateResponseForComplaint(id);
        if (response.containsKey("error")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, response.get("error"));
        }
        return ResponseEntity.ok(response);
    }

    // ================================================
    // ✅ GESTION DES ERREURS DE VALIDATION & ENUM
    // ================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("timestamp", new Date());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Erreur de validation");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("messages", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Gestion spécifique des erreurs de mapping JSON (ex: mauvaise valeur pour l'Enum Category)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("timestamp", new Date());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Format de donnée invalide");

        String msg = ex.getMessage();
        if (msg != null && msg.contains("Category")) {
            errors.put("message", "Catégorie invalide. Valeurs acceptées : TECHNIQUE, FINANCE, SUPPORT, FRAUDE, COMPTE, CREDIT, AUTRE");
        } else {
            errors.put("message", "Impossible de lire la requête. Vérifiez le format du JSON.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

