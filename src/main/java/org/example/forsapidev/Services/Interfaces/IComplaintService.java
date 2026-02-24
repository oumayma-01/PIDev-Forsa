package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;

import java.util.List;
import java.util.Map;

public interface IComplaintService {

    // CRUD
    List<Complaint> retrieveAllComplaints();
    Complaint retrieveComplaint(Long complaintId);
    Complaint addComplaint(Complaint c);
    void removeComplaint(Long complaintId);
    Complaint modifyComplaint(Complaint complaint);

    // IA
    Complaint addComplaintWithAI(Complaint c);
    Map<String, String> generateResponseForComplaint(Long complaintId);
    Map<String, Object> generateFullReport();
    Map<String, Long> getStatsByCategory();
}
