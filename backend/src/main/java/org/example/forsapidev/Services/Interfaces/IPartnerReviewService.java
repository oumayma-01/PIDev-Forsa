package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.PartnerReview;

import java.util.List;

public interface IPartnerReviewService {
    PartnerReview createReview(Long partnerId, Long clientId, Long transactionId, Integer rating, String comment);
    PartnerReview updateReview(Long reviewId, Integer rating, String comment);
    void deleteReview(Long reviewId);
    PartnerReview getReviewById(Long id);
    List<PartnerReview> getPartnerReviews(Long partnerId);
    List<PartnerReview> getClientReviews(Long clientId);
    Double getPartnerAverageRating(Long partnerId);
    Boolean canClientReview(Long clientId, Long transactionId);
    void reportReview(Long reviewId, String reason);
}