package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.Partner;
import org.example.forsapidev.entities.PartnershipManagement.PartnerStatus;
import org.example.forsapidev.entities.PartnershipManagement.PartnerType;

import java.util.List;

public interface IPartnerService {
    Partner createPartner(Partner partner);
    Partner updatePartner(Long id, Partner partner);
    Partner getPartnerById(Long id);
    Partner getPartnerByQrCode(String qrCodeId);
    List<Partner> getAllPartners();
    List<Partner> getPartnersByStatus(PartnerStatus status);
    List<Partner> getPartnersByType(PartnerType type);
    List<Partner> getPartnersByCity(String city);
    Partner activatePartner(Long id);
    Partner suspendPartner(Long id, String reason);
    Partner reactivatePartner(Long id);
    void deletePartner(Long id);
    void updatePartnerStats(Long partnerId, Double amount);
    void updatePartnerRating(Long partnerId);
    void updatePartnerBadge(Long partnerId);
    List<Partner> getTopRatedPartners();
    List<Partner> getPartnersNearby(Double latitude, Double longitude, Integer radiusKm);
}