package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.ComplaintNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintNotificationRepository extends JpaRepository<ComplaintNotification, Long> {

    List<ComplaintNotification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    List<ComplaintNotification> findByRecipientIdAndIsReadFalse(Long userId);

    long countByRecipientIdAndIsReadFalse(Long userId);
}
