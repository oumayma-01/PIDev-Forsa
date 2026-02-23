package org.example.forsapidev.Controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/complaints")
public class ComplaintController {

    // ✅ INJECTION UNIQUE : Le Service (pas les repositories !)
    private final IComplaintService complaintService;

    // ================================================
    // CRUD DE BASE
    // ================================================

    @GetMapping("/retrieve-all-complaints")
    public ResponseEntity<List<Complaint>> getComplaints() {
        List<Complaint> complaints = complaintService.retrieveAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/retrieve-complaint/{complaint-id}")
    public ResponseEntity<Complaint> retrieveComplaint(
            @PathVariable("complaint-id") Long cId) {
        Complaint complaint = complaintService.retrieveComplaint(cId);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(complaint);
    }

    // ✅ AJOUT : @Valid pour activer la validation
    @PostMapping("/add-complaint")
    public ResponseEntity<Complaint> addComplaint(
            @Valid @RequestBody Complaint c) {
        Complaint saved = complaintService.addComplaint(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ AJOUT : @Valid pour activer la validation
    @PutMapping("/modify-complaint")
    public ResponseEntity<Complaint> modifyComplaint(
            @Valid @RequestBody Complaint c) {
        Complaint updated = complaintService.modifyComplaint(c);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/remove-complaint/{complaint-id}")
    public ResponseEntity<Void> removeComplaint(
            @PathVariable("complaint-id") Long cId) {
        complaintService.removeComplaint(cId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ================================================
    // ✅ IA 1 : CRÉER AVEC CLASSIFICATION AUTOMATIQUE
    // ================================================
    @PostMapping("/add-complaint-ai")
    public ResponseEntity<Complaint> addComplaintWithAI(
            @Valid @RequestBody Complaint c) {
        Complaint saved = complaintService.addComplaintWithAI(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ================================================
    // ✅ IA 2 : GÉNÉRER UNE RÉPONSE AUTOMATIQUE
    // ================================================
    @GetMapping("/generate-response/{complaint-id}")
    public ResponseEntity<Map<String, String>> generateResponse(
            @PathVariable("complaint-id") Long cId) {
        Map<String, String> response =
                complaintService.generateResponseForComplaint(cId);

        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ================================================
    // ✅ IA 3 : GÉNÉRER UN RAPPORT COMPLET
    // ================================================
    @GetMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport() {
        Map<String, Object> report = complaintService.generateFullReport();
        return ResponseEntity.ok(report);
    }

    // ================================================
    // ✅ BONUS : STATISTIQUES PAR CATÉGORIE
    // ================================================
    @GetMapping("/stats-by-category")
    public ResponseEntity<Map<String, Long>> getStatsByCategory() {
        Map<String, Long> stats = complaintService.getStatsByCategory();
        return ResponseEntity.ok(stats);
    }

    // ================================================
    // ✅ GESTION DES ERREURS DE VALIDATION
    // ================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

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

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}