package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import java.util.List;

public interface IComplaintService {

    List<Complaint> retrieveAllComplaints();
    Complaint retrieveComplaint(Long complaintId);
    Complaint addComplaint(Complaint c);
    void removeComplaint(Long complaintId);
    Complaint modifyComplaint(Complaint complaint);
}