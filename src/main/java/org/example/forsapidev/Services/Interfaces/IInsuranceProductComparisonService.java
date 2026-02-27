package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.DTO.ComparisonResultDTO;
import java.util.List;

public interface IInsuranceProductComparisonService {
    ComparisonResultDTO compareProducts(List<Long> productIds);
}
