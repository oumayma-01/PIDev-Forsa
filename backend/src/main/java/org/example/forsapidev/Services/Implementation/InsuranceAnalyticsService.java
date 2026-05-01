package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.*;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.Repositories.InsuranceProductRepository;
import org.example.forsapidev.Services.Interfaces.IClaimsDashboardService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class InsuranceAnalyticsService {

    private final InsurancePolicyRepository policyRepository;
    private final InsuranceProductRepository productRepository;
    private final IClaimsDashboardService claimsDashboardService;

    public InsuranceAnalyticsService(InsurancePolicyRepository policyRepository, 
                                   InsuranceProductRepository productRepository,
                                   IClaimsDashboardService claimsDashboardService) {
        this.policyRepository = policyRepository;
        this.productRepository = productRepository;
        this.claimsDashboardService = claimsDashboardService;
    }

    public InsuranceOverviewDTO getGlobalOverview() {
        System.out.println("📊 Compiling Global Insurance Overview...");
        InsuranceOverviewDTO overview = new InsuranceOverviewDTO();
        
        try {
            overview.setTotalProducts(productRepository.count());
            overview.setActivePolicies(policyRepository.countActivePolicies());
            overview.setCanceledPolicies(policyRepository.countCanceledPolicies());
            overview.setPendingPolicies(policyRepository.countPendingPolicies());
            overview.setSuspendedPolicies(policyRepository.countSuspendedPolicies());
            
            Double revenue = policyRepository.getTotalActivePremiumRevenue();
            overview.setTotalPremiumRevenue(revenue != null ? revenue : 0.0);
            
            // Product Popularity
            List<ProductPopularityDTO> popularProducts = new ArrayList<>();
            try {
                List<Object[]> popularityData = policyRepository.getProductPopularityData();
                if (popularityData != null) {
                    for (Object[] row : popularityData) {
                        if (row != null && row.length >= 3 && row[0] != null) {
                            popularProducts.add(new ProductPopularityDTO(
                                row[0].toString(),
                                row[1] != null ? ((Number) row[1]).longValue() : 0L,
                                row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error fetching product popularity: " + e.getMessage());
            }
            overview.setPopularProducts(popularProducts);
            
            // Add Claims Analytics
            try {
                overview.setClaimsAnalytics(claimsDashboardService.getDashboardData());
            } catch (Exception e) {
                System.err.println("❌ Error fetching claims analytics: " + e.getMessage());
                overview.setClaimsAnalytics(new ClaimsDashboardDTO()); // Return empty instead of null
            }

            System.out.println("✅ Global Overview compiled successfully (Products: " + popularProducts.size() + 
                               ", ClaimsTrends: " + (overview.getClaimsAnalytics() != null ? overview.getClaimsAnalytics().getMonthlyTrends().size() : 0) + ")");
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in getGlobalOverview: " + e.getMessage());
            e.printStackTrace();
        }
        
        return overview;
    }
}
