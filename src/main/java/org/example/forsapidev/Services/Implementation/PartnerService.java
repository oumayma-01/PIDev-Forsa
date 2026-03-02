package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IPartnerService;
import org.example.forsapidev.entities.PartnershipManagement.*;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerRepository;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerService implements IPartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerReviewRepository reviewRepository;

    @Override
    @Transactional
    public Partner createPartner(Partner partner) {
        log.info("Creating new partner: {}", partner.getBusinessName());

        partner.setQrCodeId("PTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        partner.setStatus(PartnerStatus.PENDING);
        partner.setTotalAmountProcessed(0.0);
        partner.setTotalTransactionsCount(0);
        partner.setAverageRating(0.0);
        partner.setTotalReviews(0);
        partner.setBadge(PartnerBadge.BRONZE);

        return partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public Partner updatePartner(Long id, Partner partner) {
        log.info("Updating partner: {}", id);

        Partner existing = getPartnerById(id);

        existing.setBusinessName(partner.getBusinessName());
        existing.setPartnerType(partner.getPartnerType());
        existing.setAddress(partner.getAddress());
        existing.setCity(partner.getCity());
        existing.setBusinessPhone(partner.getBusinessPhone());
        existing.setBusinessEmail(partner.getBusinessEmail());
        existing.setDescription(partner.getDescription());
        existing.setContactPersonName(partner.getContactPersonName());
        existing.setContactPersonPhone(partner.getContactPersonPhone());
        existing.setContactPersonEmail(partner.getContactPersonEmail());
        existing.setLatitude(partner.getLatitude());
        existing.setLongitude(partner.getLongitude());

        return partnerRepository.save(existing);
    }

    @Override
    public Partner getPartnerById(Long id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found: " + id));
    }

    @Override
    public Partner getPartnerByQrCode(String qrCodeId) {
        return partnerRepository.findByQrCodeId(qrCodeId)
                .orElseThrow(() -> new RuntimeException("Partner not found with QR: " + qrCodeId));
    }

    @Override
    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    @Override
    public List<Partner> getPartnersByStatus(PartnerStatus status) {
        return partnerRepository.findByStatus(status);
    }

    @Override
    public List<Partner> getPartnersByType(PartnerType type) {
        return partnerRepository.findByPartnerType(type);
    }

    @Override
    public List<Partner> getPartnersByCity(String city) {
        return partnerRepository.findByCity(city);
    }

    @Override
    @Transactional
    public Partner activatePartner(Long id) {
        log.info("Activating partner: {}", id);

        Partner partner = getPartnerById(id);
        partner.setStatus(PartnerStatus.ACTIVE);
        partner.setActivatedAt(LocalDateTime.now());

        return partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public Partner suspendPartner(Long id, String reason) {
        log.info("Suspending partner: {} for reason: {}", id, reason);

        Partner partner = getPartnerById(id);
        partner.setStatus(PartnerStatus.SUSPENDED);
        partner.setSuspendedAt(LocalDateTime.now());
        partner.setSuspensionReason(reason);

        return partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public Partner reactivatePartner(Long id) {
        log.info("Reactivating partner: {}", id);

        Partner partner = getPartnerById(id);
        partner.setStatus(PartnerStatus.ACTIVE);
        partner.setSuspendedAt(null);
        partner.setSuspensionReason(null);

        return partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public void deletePartner(Long id) {
        log.info("Deleting partner: {}", id);
        partnerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updatePartnerStats(Long partnerId, Double amount) {
        Partner partner = getPartnerById(partnerId);

        partner.setTotalAmountProcessed(partner.getTotalAmountProcessed() + amount);
        partner.setTotalTransactionsCount(partner.getTotalTransactionsCount() + 1);

        partnerRepository.save(partner);

        updatePartnerBadge(partnerId);
    }

    @Override
    @Transactional
    public void updatePartnerRating(Long partnerId) {
        Partner partner = getPartnerById(partnerId);

        Double avgRating = reviewRepository.getAverageRating(partnerId);
        Integer totalReviews = reviewRepository.getTotalReviews(partnerId);

        partner.setAverageRating(avgRating != null ? avgRating : 0.0);
        partner.setTotalReviews(totalReviews != null ? totalReviews : 0);

        partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public void updatePartnerBadge(Long partnerId) {
        Partner partner = getPartnerById(partnerId);

        Integer transactionCount = partner.getTotalTransactionsCount();
        PartnerBadge newBadge;
        Double newCommissionRate;

        if (transactionCount >= 500) {
            newBadge = PartnerBadge.DIAMOND;
            newCommissionRate = 0.035;
        } else if (transactionCount >= 201) {
            newBadge = PartnerBadge.GOLD;
            newCommissionRate = 0.03;
        } else if (transactionCount >= 51) {
            newBadge = PartnerBadge.SILVER;
            newCommissionRate = 0.025;
        } else {
            newBadge = PartnerBadge.BRONZE;
            newCommissionRate = 0.02;
        }

        if (partner.getBadge() != newBadge) {
            log.info("Partner {} upgraded to {} badge", partnerId, newBadge);
            partner.setBadge(newBadge);
            partner.setCommissionRate(newCommissionRate);
            partnerRepository.save(partner);
        }
    }

    @Override
    public List<Partner> getTopRatedPartners() {
        return partnerRepository.findTopRatedPartners();
    }

    @Override
    public List<Partner> getPartnersNearby(Double latitude, Double longitude, Integer radiusKm) {
        List<Partner> allPartners = partnerRepository.findAllWithLocation();

        return allPartners.stream()
                .filter(p -> {
                    Double distance = calculateDistance(
                            latitude, longitude,
                            p.getLatitude(), p.getLongitude()
                    );
                    return distance <= radiusKm;
                })
                .sorted((p1, p2) -> {
                    Double d1 = calculateDistance(latitude, longitude, p1.getLatitude(), p1.getLongitude());
                    Double d2 = calculateDistance(latitude, longitude, p2.getLatitude(), p2.getLongitude());
                    return d1.compareTo(d2);
                })
                .collect(Collectors.toList());
    }

    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}