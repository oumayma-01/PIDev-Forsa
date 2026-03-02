package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;

import java.util.List;
import java.util.Map;

public interface IComplaintService {

    List<Complaint> retrieveAllComplaints();
    Complaint retrieveComplaint(Long complaintId);
    Complaint addComplaint(Complaint c);
    void removeComplaint(Long complaintId);
    Complaint modifyComplaint(Complaint complaint);

    Complaint addComplaintWithAI(Complaint c);
    Map<String, String> generateResponseForComplaint(Long complaintId);

    Map<String, Object> generateFullReportWithAI();
    Map<String, Object> getComplaintSummaryReport();
    List<Map<String, Object>> getComplaintTrendsLastMonths(int months);

    Map<String, Long> getStatsByCategory();

    Complaint affectComplaintToUser(Long complaintId, Long userId);
    Response addResponseAndUpdateStatus(Long complaintId, Response r);
    void closeComplaintIfEligible(Long complaintId);
    Map<String, Long> getStatsByPriority();
}
