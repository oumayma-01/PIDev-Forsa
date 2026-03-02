package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Getter
@Setter
public class ComplaintController {

    private final IComplaintService complaintService;

    // ========== CRUD de base ==========

    @GetMapping("/retrieve-all-complaints")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public List<Complaint> getComplaints() {
        return complaintService.retrieveAllComplaints();
    }

//    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMIN')")
    @GetMapping("/retrieve-complaint/{complaint-id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Complaint retrieveComplaint(@PathVariable("complaint-id") Long cId) {
        return complaintService.retrieveComplaint(cId);
    }
//    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add-complaint")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public Complaint addComplaint(@RequestBody Complaint c) {
        return complaintService.addComplaint(c);
    }
//    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/remove-complaint/{complaint-id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeComplaint(@PathVariable("complaint-id") Long cId) {
        complaintService.removeComplaint(cId);
    }
//    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PutMapping("/modify-complaint")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Complaint modifyComplaint(@RequestBody Complaint c) {
        return complaintService.modifyComplaint(c);
    }

    // ========== Fonctions IA ==========

    @PostMapping("/add-complaint-ai")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public Complaint addComplaintWithAI(@RequestBody Complaint c) {
        return complaintService.addComplaintWithAI(c);
    }

    @GetMapping("/{complaintId}/ai-response")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Map<String, String> generateResponseForComplaint(@PathVariable Long complaintId) {
        return complaintService.generateResponseForComplaint(complaintId);
    }

    @GetMapping("/ai-full-report")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Map<String, Object> generateFullReportWithAI() {
        return complaintService.generateFullReportWithAI();
    }

    // ========== Stats & reporting ==========

    @GetMapping("/summary-report")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Map<String, Object> getComplaintSummaryReport() {
        return complaintService.getComplaintSummaryReport();
    }

    @GetMapping("/trends-last-months")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public List<Map<String, Object>> getComplaintTrendsLastMonths(
            @RequestParam(defaultValue = "6") int months) {
        return complaintService.getComplaintTrendsLastMonths(months);
    }

    @GetMapping("/stats-by-category")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Map<String, Long> getStatsByCategory() {
        return complaintService.getStatsByCategory();
    }

    // ========== Affectation et réponses ==========

    @PostMapping("/{complaintId}/assign/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Complaint affectComplaintToUser(@PathVariable Long complaintId,
                                           @PathVariable Long userId) {
        return complaintService.affectComplaintToUser(complaintId, userId);
    }

    @PostMapping("/{complaintId}/responses")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public Response addResponseAndUpdateStatus(@PathVariable Long complaintId,
                                               @RequestBody Response r) {
        return complaintService.addResponseAndUpdateStatus(complaintId, r);
    }

    @PostMapping("/{complaintId}/close")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public void closeComplaintIfEligible(@PathVariable Long complaintId) {
        complaintService.closeComplaintIfEligible(complaintId);
    }
}
