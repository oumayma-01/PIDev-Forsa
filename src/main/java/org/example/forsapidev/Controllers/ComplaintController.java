package org.example.forsapidev.Controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
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

    private final IComplaintService complaintService;

    // =========================
    // CRUD
    // =========================
    @GetMapping("/retrieve-all-complaints")
    public ResponseEntity<List<Complaint>> getComplaints() {
        return ResponseEntity.ok(complaintService.retrieveAllComplaints());
    }

    @GetMapping("/retrieve-complaint/{complaint-id}")
    public ResponseEntity<Complaint> retrieveComplaint(@PathVariable("complaint-id") Long cId) {
        Complaint complaint = complaintService.retrieveComplaint(cId);
        if (complaint == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(complaint);
    }

    @PostMapping("/add-complaint")
    public ResponseEntity<Complaint> addComplaint(@Valid @RequestBody Complaint c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addComplaint(c));
    }

    @PutMapping("/modify-complaint")
    public ResponseEntity<Complaint> modifyComplaint(@Valid @RequestBody Complaint c) {
        return ResponseEntity.ok(complaintService.modifyComplaint(c));
    }

    @DeleteMapping("/remove-complaint/{complaint-id}")
    public ResponseEntity<Void> removeComplaint(@PathVariable("complaint-id") Long cId) {
        complaintService.removeComplaint(cId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // =========================
    // IA
    // =========================
    @PostMapping("/add-complaint-ai")
    public ResponseEntity<Complaint> addComplaintWithAI(@Valid @RequestBody Complaint c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addComplaintWithAI(c));
    }

    @GetMapping("/generate-response/{complaint-id}")
    public ResponseEntity<Map<String, String>> generateResponse(@PathVariable("complaint-id") Long cId) {
        Map<String, String> response = complaintService.generateResponseForComplaint(cId);
        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ✅ Un seul endpoint rapport (simple ou IA)
    @GetMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestParam(defaultValue = "false") boolean ai) {
        return ai
                ? ResponseEntity.ok(complaintService.generateFullReportWithAI())
                : ResponseEntity.ok(complaintService.generateFullReport());
    }

    // =========================
    // Stats / Reporting
    // =========================
    @GetMapping("/stats-by-category")
    public ResponseEntity<Map<String, Long>> getStatsByCategory() {
        return ResponseEntity.ok(complaintService.getStatsByCategory());
    }

    @GetMapping("/report/summary")
    public ResponseEntity<Map<String, Object>> complaintSummaryReport() {
        return ResponseEntity.ok(complaintService.getComplaintSummaryReport());
    }

    @GetMapping("/report/trends")
    public ResponseEntity<List<Map<String, Object>>> complaintTrends(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(complaintService.getComplaintTrendsLastMonths(months));
    }

    // =========================
    // Affect
    // =========================
    @PutMapping("/affect/{complaint-id}/to-user/{user-id}")
    public ResponseEntity<Complaint> affectComplaintToUser(
            @PathVariable("complaint-id") Long cId,
            @PathVariable("user-id") Long uId) {

        Complaint updated = complaintService.affectComplaintToUser(cId, uId);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(updated);
    }

    // =========================
    // Workflow
    // =========================
    @PostMapping("/{complaint-id}/responses")
    public ResponseEntity<Response> addResponseToComplaint(
            @PathVariable("complaint-id") Long cId,
            @Valid @RequestBody Response r) {

        Response saved = complaintService.addResponseAndUpdateStatus(cId, r);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{complaint-id}/close")
    public ResponseEntity<Map<String, Object>> closeComplaint(@PathVariable("complaint-id") Long cId) {
        complaintService.closeComplaintIfEligible(cId);
        return ResponseEntity.ok(Map.of("message", "Complaint closed successfully", "complaintId", cId));
    }

    // =========================
    // Validation errors
    // =========================
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
}
