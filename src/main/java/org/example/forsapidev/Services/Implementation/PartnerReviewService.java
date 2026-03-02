package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IPartnerReviewService;
import org.example.forsapidev.Services.Interfaces.IPartnerService;
import org.example.forsapidev.entities.PartnershipManagement.PartnerReview;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerReviewService implements IPartnerReviewService {

    private final PartnerReviewRepository reviewRepository;
    private final IPartnerService partnerService;

    @Override
    @Transactional
    public PartnerReview createReview(Long partnerId, Long clientId, Long transactionId, Integer rating, String comment) {
        log.info("Creating review for partner: {} by client: {}", partnerId, clientId);

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        if (reviewRepository.existsByTransactionId(transactionId)) {
            throw new RuntimeException("Transaction already reviewed");
        }

        PartnerReview review = PartnerReview.builder()
                .partnerId(partnerId)
                .clientId(clientId)
                .transactionId(transactionId)
                .rating(rating)
                .comment(comment)
                .isVerified(true)
                .build();

        PartnerReview saved = reviewRepository.save(review);

        partnerService.updatePartnerRating(partnerId);

        return saved;
    }

    @Override
    @Transactional
    public PartnerReview updateReview(Long reviewId, Integer rating, String comment) {
        log.info("Updating review: {}", reviewId);

        PartnerReview review = getReviewById(reviewId);

        if (rating != null && rating >= 1 && rating <= 5) {
            review.setRating(rating);
        }

        if (comment != null) {
            review.setComment(comment);
        }

        PartnerReview updated = reviewRepository.save(review);

        partnerService.updatePartnerRating(review.getPartnerId());

        return updated;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("Deleting review: {}", reviewId);

        PartnerReview review = getReviewById(reviewId);
        Long partnerId = review.getPartnerId();

        reviewRepository.deleteById(reviewId);

        partnerService.updatePartnerRating(partnerId);
    }

    @Override
    public PartnerReview getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }

    @Override
    public List<PartnerReview> getPartnerReviews(Long partnerId) {
        return reviewRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId);
    }

    @Override
    public List<PartnerReview> getClientReviews(Long clientId) {
        return reviewRepository.findByClientId(clientId);
    }

    @Override
    public Double getPartnerAverageRating(Long partnerId) {
        Double avg = reviewRepository.getAverageRating(partnerId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    public Boolean canClientReview(Long clientId, Long transactionId) {
        return !reviewRepository.existsByTransactionId(transactionId);
    }

    @Override
    @Transactional
    public void reportReview(Long reviewId, String reason) {
        log.info("Reporting review: {} for reason: {}", reviewId, reason);

        PartnerReview review = getReviewById(reviewId);
        review.setIsReported(true);
        review.setReportReason(reason);

        reviewRepository.save(review);
    }
}