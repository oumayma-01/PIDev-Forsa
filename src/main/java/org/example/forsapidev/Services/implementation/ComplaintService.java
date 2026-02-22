package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService implements IComplaintService {

    private final ComplaintRepository complaintRepository;

    public List<Complaint> retrieveAllComplaints() {
        return complaintRepository.findAll();
    }

    public Complaint retrieveComplaint(Long complaintId) {
        return complaintRepository.findById(complaintId).orElse(null);
    }

    public Complaint addComplaint(Complaint c) {
        c.setCreatedAt(new Date()); // On initialise la date
        return complaintRepository.save(c);
    }

    public void removeComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
    }

    public Complaint modifyComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint addComplaintWithAI(Complaint c) {
        // --- MÉTIER : AUDITEUR DE BIAIS ---
        String desc = (c.getDescription() != null) ? c.getDescription().toLowerCase() : "";

        // Tri automatique par IA (Simulation)
        if (desc.contains("argent") || desc.contains("paiement") || desc.contains("remboursement")) {
            c.setCategory("FINANCE");
        } else if (desc.contains("connexion") || desc.contains("bug") || desc.contains("application")) {
            c.setCategory("TECHNIQUE");
        } else {
            c.setCategory("SUPPORT_GENERAL");
        }

        c.setSubject("Analyse IA : " + (c.getSubject() != null ? c.getSubject() : "Nouveau ticket"));

        // CORRECTION ICI : Ton entité utilise String, donc on met une chaîne de caractères
        c.setStatus("OPEN");
        c.setCreatedAt(new Date());

        return complaintRepository.save(c);
    }

    @Override
    public Map<String, String> generateResponseForComplaint(Long complaintId) {
        // --- MÉTIER : CONVERSATION DESIGNER ---
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        if (c == null) return Map.of("error", "Plainte non trouvée");

        String responseText = "Bonjour, l'analyse de votre requête concernant '" + c.getCategory() +
                "' est terminée. Notre équipe traitera votre message : " + c.getDescription();

        return Map.of("response", responseText);
    }

    @Override
    public Map<String, Object> generateFullReport() {
        // --- MÉTIER : ARCHITECTE ÉMOTIONNEL ---
        long total = complaintRepository.count();
        return Map.of(
                "title", "Rapport de Fidélité Client",
                "total_tickets", total,
                "status", "Analyse effectuée avec succès"
        );
    }

    @Override
    public Map<String, Long> getStatsByCategory() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));
    }
}