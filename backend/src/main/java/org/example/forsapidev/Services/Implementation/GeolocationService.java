package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.IGeolocationService;
import org.example.forsapidev.entities.PartnershipManagement.Partner;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeolocationService implements IGeolocationService {

    private final PartnerRepository partnerRepository;

    @Override
    public List<Partner> findPartnersNearby(Double latitude, Double longitude, Integer radiusKm) {
        log.info("Finding partners near: {}, {} within {}km", latitude, longitude, radiusKm);

        List<Partner> allPartners = partnerRepository.findAllWithLocation();

        return allPartners.stream()
                .filter(partner -> {
                    Double distance = calculateDistance(
                            latitude, longitude,
                            partner.getLatitude(), partner.getLongitude()
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

    @Override
    public Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String getAddressFromCoordinates(Double latitude, Double longitude) {
        return String.format("Lat: %.6f, Lon: %.6f", latitude, longitude);
    }

    @Override
    public Map<String, Double> getCoordinatesFromAddress(String address) {
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("latitude", 36.8065);
        coordinates.put("longitude", 10.1815);
        return coordinates;
    }
}