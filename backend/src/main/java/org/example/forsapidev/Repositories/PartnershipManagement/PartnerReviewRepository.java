package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.PartnerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerReviewRepository extends JpaRepository<PartnerReview, Long> {

    List<PartnerReview> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    List<PartnerReview> findByClientId(Long clientId);

    @Query("SELECT AVG(r.rating) FROM PartnerReview r WHERE r.partnerId = :partnerId")
    Double getAverageRating(@Param("partnerId") Long partnerId);

    @Query("SELECT COUNT(r) FROM PartnerReview r WHERE r.partnerId = :partnerId")
    Integer getTotalReviews(@Param("partnerId") Long partnerId);

    @Query("SELECT r FROM PartnerReview r WHERE r.partnerId = :partnerId " +
            "AND r.rating >= :minRating ORDER BY r.createdAt DESC")
    List<PartnerReview> findHighRatedReviews(@Param("partnerId") Long partnerId,
                                             @Param("minRating") Integer minRating);

    Boolean existsByTransactionId(Long transactionId);
}