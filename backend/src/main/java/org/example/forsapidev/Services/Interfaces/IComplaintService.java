package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.DTO.ComplaintCreditEligibilityDTO;
import org.example.forsapidev.DTO.ComplaintFinancialImpactDTO;

import java.util.List;
import java.util.Map;

public interface IComplaintService {

    List<Complaint> retrieveAllComplaints();
    Complaint retrieveComplaint(Long complaintId);
    Complaint addComplaint(Complaint c);
    List<Complaint> getComplaintsByUsername(String username);
    void removeComplaint(Long complaintId);
    Complaint modifyComplaint(Complaint complaint);

    Complaint addComplaintWithAI(Complaint c);  // inclut maintenant la priorité
    Map<String, String> generateResponseForComplaint(Long complaintId);

    Map<String, Object> generateFullReportWithAI();
    Map<String, Object> getComplaintSummaryReport();
    List<Map<String, Object>> getComplaintTrendsLastMonths(int months);

    Map<String, Long> getStatsByCategory();
    Map<String, Long> getStatsByPriority();  // NOUVEAU

    Complaint affectComplaintToUser(Long complaintId, Long userId);
    Response addResponseAndUpdateStatus(Long complaintId, Response r);
    void closeComplaintIfEligible(Long complaintId);

    ComplaintCreditEligibilityDTO getCreditEligibilityByComplaint(Long complaintId, Double requiredScore);
    ComplaintFinancialImpactDTO getFinancialImpactByComplaint(Long complaintId);
}
