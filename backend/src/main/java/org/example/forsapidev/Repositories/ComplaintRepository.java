package org.example.forsapidev.Repositories;

import org.example.forsapidev.DTO.PriorityStatsDTO;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Category;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserId(Long userId);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByCategory(Category category);

    @Query("SELECT new org.example.forsapidev.DTO.PriorityStatsDTO(c.priority, COUNT(c)) " +
            "FROM Complaint c " +
            "WHERE c.priority IS NOT NULL " +
            "GROUP BY c.priority")
    List<PriorityStatsDTO> statsByPriority();
}