package org.example.forsapidev.Services.implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService implements IComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintAiAssistant complaintAiAssistant;

    @Override
    public List<Complaint> retrieveAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint retrieveComplaint(Long complaintId) {
        return complaintRepository.findById(complaintId).orElse(null);
    }

    @Override
    public Complaint addComplaint(Complaint c) {
        c.setCreatedAt(new Date());
        return complaintRepository.save(c);
    }

    @Override
    public void removeComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
    }

    @Override
    public Complaint modifyComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    // ✅ IA 1 : classification via LLM
    @Override
    public Complaint addComplaintWithAI(Complaint c) {
        String category = complaintAiAssistant.classifyCategory(c.getDescription());

        c.setCategory(category);
        c.setSubject("Analyse IA : " + (c.getSubject() != null ? c.getSubject() : "Nouveau ticket"));
        c.setStatus("OPEN");
        c.setCreatedAt(new Date());

        return complaintRepository.save(c);
    }

    // ✅ IA 2 : réponse via LLM
    @Override
    public Map<String, String> generateResponseForComplaint(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        if (c == null) return Map.of("error", "Plainte non trouvée");

        String responseText = complaintAiAssistant.draftResponse(
                c.getCategory(),
                c.getSubject(),
                c.getDescription()
        );

        return Map.of("response", responseText);
    }

    @Override
    public Map<String, Object> generateFullReport() {
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
