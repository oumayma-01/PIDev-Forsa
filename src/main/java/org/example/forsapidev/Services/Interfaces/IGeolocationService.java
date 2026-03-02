package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.Partner;

import java.util.List;
import java.util.Map;

public interface IGeolocationService {
    List<Partner> findPartnersNearby(Double latitude, Double longitude, Integer radiusKm);
    Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2);
    String getAddressFromCoordinates(Double latitude, Double longitude);
    Map<String, Double> getCoordinatesFromAddress(String address);
}