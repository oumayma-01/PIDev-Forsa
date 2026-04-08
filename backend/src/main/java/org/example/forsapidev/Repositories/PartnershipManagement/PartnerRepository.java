package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.Partner;
import org.example.forsapidev.entities.PartnershipManagement.PartnerBadge;
import org.example.forsapidev.entities.PartnershipManagement.PartnerStatus;
import org.example.forsapidev.entities.PartnershipManagement.PartnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByQrCodeId(String qrCodeId);

    Optional<Partner> findByRegistrationNumber(String registrationNumber);

    List<Partner> findByStatus(PartnerStatus status);

    List<Partner> findByPartnerType(PartnerType partnerType);

    List<Partner> findByCity(String city);

    List<Partner> findByBadge(PartnerBadge badge);

    @Query("SELECT p FROM Partner p WHERE p.status = 'ACTIVE' " +
            "ORDER BY p.averageRating DESC, p.totalReviews DESC")
    List<Partner> findTopRatedPartners();

    @Query("SELECT p FROM Partner p WHERE p.status = 'ACTIVE' " +
            "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
    List<Partner> findAllWithLocation();

    @Query("SELECT COUNT(p) FROM Partner p WHERE p.status = :status")
    Long countByStatus(@Param("status") PartnerStatus status);

    @Query("SELECT SUM(p.totalAmountProcessed) FROM Partner p WHERE p.status = 'ACTIVE'")
    Double getTotalVolumeProcessed();
}