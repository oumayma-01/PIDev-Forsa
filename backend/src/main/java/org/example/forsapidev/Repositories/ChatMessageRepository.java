package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByPolicyIdOrderByTimestampAsc(Long policyId);
    
    // For conversation history (last 10)
    List<ChatMessage> findTop10ByPolicyIdOrderByTimestampDesc(Long policyId);
    
    // For rate limiting
    long countByPolicyIdAndTimestampAfter(Long policyId, LocalDateTime timestamp);
}
