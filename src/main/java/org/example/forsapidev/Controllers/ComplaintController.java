package org.example.forsapidev.Controllers;


import lombok.AllArgsConstructor;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public List<Complaint> getComplaints() {
        return complaintService.retrieveAllComplaints();
    }

    @GetMapping("/retrieve-complaint/{complaint-id}")
    public Complaint retrieveComplaint(@PathVariable("complaint-id") Long cId) {
        return complaintService.retrieveComplaint(cId);
    }

    @PostMapping("/add-complaint")
    public Complaint addComplaint(@RequestBody Complaint c) {
        return complaintService.addComplaint(c);
    }

    @PutMapping("/modify-complaint")
    public Complaint modifyComplaint(@RequestBody Complaint c) {
        return complaintService.modifyComplaint(c);
    }

    @DeleteMapping("/remove-complaint/{complaint-id}")
    public void removeComplaint(@PathVariable("complaint-id") Long cId) {
        complaintService.removeComplaint(cId);
    }

    // ================================================
    // ✅ IA 1 : CRÉER AVEC CLASSIFICATION AUTOMATIQUE
    // ================================================
    @PostMapping("/add-complaint-ai")
    public Complaint addComplaintWithAI(@RequestBody Complaint c) {
        return complaintService.addComplaintWithAI(c);
    }

    // ================================================
    // ✅ IA 2 : GÉNÉRER UNE RÉPONSE AUTOMATIQUE
    // ================================================
    @GetMapping("/generate-response/{complaint-id}")
    public Map<String, String> generateResponse(
            @PathVariable("complaint-id") Long cId) {
        return complaintService.generateResponseForComplaint(cId);
    }

    // ================================================
    // ✅ IA 3 : GÉNÉRER UN RAPPORT COMPLET
    // ================================================
    @GetMapping("/generate-report")
    public Map<String, Object> generateReport() {
        return complaintService.generateFullReport();
    }

    // ================================================
    // ✅ BONUS : STATISTIQUES PAR CATÉGORIE
    // ================================================
    @GetMapping("/stats-by-category")
    public Map<String, Long> getStatsByCategory() {
        return complaintService.getStatsByCategory();
    }
}