package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByComplaintId(Long complaintId);

    @Query("select r.responseStatus, count(r) from Response r where r.responseStatus is not null group by r.responseStatus")
    List<Object[]> countByResponseStatus();
}
