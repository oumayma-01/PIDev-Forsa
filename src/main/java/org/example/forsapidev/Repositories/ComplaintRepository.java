package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserId(Long userId);
    List<Complaint> findByStatus(String status);
    List<Complaint> findByCategory(String category);

    @Query("select c.status, count(c) from Complaint c group by c.status")
    List<Object[]> countByStatus();

    @Query("select c.category, count(c) from Complaint c where c.category is not null group by c.category")
    List<Object[]> countByCategory();
}
