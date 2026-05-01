package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.ComplaintNotificationRepository;
import org.example.forsapidev.Repositories.ComplaintRepository;
import org.example.forsapidev.Services.Interfaces.IComplaintNotificationService;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.ComplaintNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintNotificationService implements IComplaintNotificationService {

    private final ComplaintNotificationRepository notificationRepository;
    private final ComplaintRepository complaintRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private void publishRealtime(ComplaintNotification notification) {
        if (notification == null || notification.getRecipient() == null || notification.getRecipient().getUsername() == null) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getUsername(),
                "/queue/complaint-notifications",
                notification
        );
    }

    @Override
    public void notifyClientResponseAdded(Long complaintId, String responseMessage) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null || complaint.getUser() == null) return;

        ComplaintNotification notification = new ComplaintNotification();
        notification.setRecipient(complaint.getUser());
        notification.setComplaint(complaint);
        notification.setTitle("New response to your complaint");
        String safeMessage = responseMessage == null ? "" : responseMessage;
        notification.setMessage("Your complaint \"" + complaint.getSubject() +
                "\" received a new response: " +
                safeMessage.substring(0, Math.min(safeMessage.length(), 100)));
        notification.setType(ComplaintNotification.NotificationType.RESPONSE_ADDED);
        ComplaintNotification saved = notificationRepository.save(notification);
        publishRealtime(saved);
    }

    @Override
    public void notifyClientComplaintClosed(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null || complaint.getUser() == null) return;

        ComplaintNotification notification = new ComplaintNotification();
        notification.setRecipient(complaint.getUser());
        notification.setComplaint(complaint);
        notification.setTitle("Complaint closed");
        notification.setMessage("Your complaint \"" + complaint.getSubject() +
                "\" has been closed.");
        notification.setType(ComplaintNotification.NotificationType.COMPLAINT_CLOSED);
        ComplaintNotification saved = notificationRepository.save(notification);
        publishRealtime(saved);
    }

    @Override
    public void notifyClientStatusChanged(Long complaintId, String newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null || complaint.getUser() == null) return;

        ComplaintNotification notification = new ComplaintNotification();
        notification.setRecipient(complaint.getUser());
        notification.setComplaint(complaint);
        notification.setTitle("Complaint status updated");
        notification.setMessage("Your complaint \"" + complaint.getSubject() +
                "\" status changed to: " + newStatus);
        notification.setType(ComplaintNotification.NotificationType.COMPLAINT_STATUS_CHANGED);
        ComplaintNotification saved = notificationRepository.save(notification);
        publishRealtime(saved);
    }

    @Override
    public List<ComplaintNotification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<ComplaintNotification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public long countUnreadForUser(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsReadForUser(Long userId) {
        List<ComplaintNotification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
