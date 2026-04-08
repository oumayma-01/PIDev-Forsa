package org.example.forsapidev.DTO;

import java.util.List;

public class ComparisonResultDTO {
    private List<InsuranceProductComparisonDTO> products;
    private Long bestValueProductId;
    private Long lowestPremiumProductId;
    private Long highestCoverageProductId;
    private String comparisonSummary;

    // Constructor
    public ComparisonResultDTO() {}

    // Getters and Setters
    public List<InsuranceProductComparisonDTO> getProducts() { return products; }
    public void setProducts(List<InsuranceProductComparisonDTO> products) { this.products = products; }

    public Long getBestValueProductId() { return bestValueProductId; }
    public void setBestValueProductId(Long bestValueProductId) { this.bestValueProductId = bestValueProductId; }

    public Long getLowestPremiumProductId() { return lowestPremiumProductId; }
    public void setLowestPremiumProductId(Long lowestPremiumProductId) { this.lowestPremiumProductId = lowestPremiumProductId; }

    public Long getHighestCoverageProductId() { return highestCoverageProductId; }
    public void setHighestCoverageProductId(Long highestCoverageProductId) { this.highestCoverageProductId = highestCoverageProductId; }

    public String getComparisonSummary() { return comparisonSummary; }
    public void setComparisonSummary(String comparisonSummary) { this.comparisonSummary = comparisonSummary; }
}