package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.ComplaintNotification;

import java.util.List;

public interface IComplaintNotificationService {
    void notifyClientResponseAdded(Long complaintId, String responseMessage);
    void notifyClientComplaintClosed(Long complaintId);
    void notifyClientStatusChanged(Long complaintId, String newStatus);
    List<ComplaintNotification> getNotificationsForUser(Long userId);
    List<ComplaintNotification> getUnreadNotificationsForUser(Long userId);
    long countUnreadForUser(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsReadForUser(Long userId);
}
