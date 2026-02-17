package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IComplaintService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.springframework.stereotype.Service;
import java.util.List;

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
        return complaintRepository.save(c);
    }

    public void removeComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
    }

    public Complaint modifyComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }
}