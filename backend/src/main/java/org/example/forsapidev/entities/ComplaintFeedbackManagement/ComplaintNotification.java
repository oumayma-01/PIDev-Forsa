package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.User;

import java.util.Date;

@Entity
@Table(name = "complaint_notification")
@Getter
@Setter
public class ComplaintNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User recipient;

    @ManyToOne
    private Complaint complaint;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean isRead;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public enum NotificationType {
        RESPONSE_ADDED,
        COMPLAINT_CLOSED,
        COMPLAINT_STATUS_CHANGED,
        FEEDBACK_REQUESTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        isRead = false;
    }
}
