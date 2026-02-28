package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;

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

    // Report (simple / IA)
    Map<String, Object> generateFullReport();
    Map<String, Object> generateFullReportWithAI();

    // Stats / Reporting
    Map<String, Long> getStatsByCategory();
    Map<String, Object> getComplaintSummaryReport();
    List<Map<String, Object>> getComplaintTrendsLastMonths(int months);

    // Affect
    Complaint affectComplaintToUser(Long complaintId, Long userId);

    // Workflow
    Response addResponseAndUpdateStatus(Long complaintId, Response r);
    void closeComplaintIfEligible(Long complaintId);
}
