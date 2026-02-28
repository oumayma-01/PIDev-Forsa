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

    @Query("select avg(f.rating) from Feedback f")
    Double avgRating();

    @Query("select f.rating, count(f) from Feedback f group by f.rating order by f.rating")
    List<Object[]> countByRating();

    @Query("select f.satisfactionLevel, count(f) from Feedback f where f.satisfactionLevel is not null group by f.satisfactionLevel")
    List<Object[]> countBySatisfactionLevel();

    @Query("select f.isAnonymous, count(f) from Feedback f group by f.isAnonymous")
    List<Object[]> countByAnonymous();
}
