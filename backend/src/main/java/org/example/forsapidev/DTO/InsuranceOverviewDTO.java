package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceOverviewDTO {
    private long totalProducts;
    private long activePolicies;
    private long canceledPolicies;
    private long pendingPolicies;
    private long suspendedPolicies;
    private double totalPremiumRevenue;
    
    private List<ProductPopularityDTO> popularProducts = new java.util.ArrayList<>();
    private ClaimsDashboardDTO claimsAnalytics = new ClaimsDashboardDTO();
}
