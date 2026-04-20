package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByComplaintId(Long complaintId);
    List<Feedback> findByComplaintUserUsername(String username);
    List<Feedback> findByComplaintUserUsernameOrderByCreatedAtDesc(String username);

    @Query("select avg(f.rating) from Feedback f")
    Double avgRating();

    @Query("""
           select c.category, avg(f.rating)
           from Feedback f
           join f.complaint c
           where f.rating is not null and c.category is not null
           group by c.category
           """)
    List<Object[]> avgRatingByComplaintCategory();
}
