package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.*;
import org.example.forsapidev.Repositories.InsuranceClaimRepository;
import org.example.forsapidev.Services.Interfaces.IClaimsDashboardService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ClaimsDashboardService implements IClaimsDashboardService {

    private final InsuranceClaimRepository claimRepository;

    public ClaimsDashboardService(InsuranceClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public ClaimsDashboardDTO getDashboardData() {
        ClaimsDashboardDTO dashboard = new ClaimsDashboardDTO();

        // 1. Get total claims and amounts
        Long totalClaims = claimRepository.getTotalClaimsCount();
        Double totalAmount = claimRepository.getTotalClaimAmount();

        dashboard.setTotalClaims(totalClaims != null ? totalClaims : 0L);
        dashboard.setTotalClaimAmount(totalAmount != null ? totalAmount : 0.0);

        // Calculate average
        if (totalClaims != null && totalClaims > 0 && totalAmount != null) {
            dashboard.setAverageClaimAmount(totalAmount / totalClaims);
        } else {
            dashboard.setAverageClaimAmount(0.0);
        }

        // 2. Calculate approval rate
        Long approvedCount = claimRepository.getApprovedClaimsCount();
        if (totalClaims != null && totalClaims > 0) {
            dashboard.setApprovalRate((approvedCount.doubleValue() / totalClaims) * 100);
        } else {
            dashboard.setApprovalRate(0.0);
        }

        // 3. Claims by status
        List<ClaimsByStatusDTO> claimsByStatus = new ArrayList<>();
        List<Object[]> statusData = claimRepository.countClaimsByStatus();
        for (Object[] row : statusData) {
            claimsByStatus.add(new ClaimsByStatusDTO(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
            ));
        }
        dashboard.setClaimsByStatus(claimsByStatus);

        // 4. Claims by policy type
        List<ClaimsByTypeDTO> claimsByType = new ArrayList<>();
        List<Object[]> typeData = claimRepository.countClaimsByPolicyType();
        for (Object[] row : typeData) {
            String policyType = row[0] != null ? row[0].toString() : "Unknown";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double total = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            claimsByType.add(new ClaimsByTypeDTO(policyType, count, total));
        }
        dashboard.setClaimsByType(claimsByType);

        // 5. Monthly trends
        List<MonthlyClaimTrendDTO> monthlyTrends = new ArrayList<>();
        List<Object[]> trendData = claimRepository.getMonthlyClaimTrends();
        for (Object[] row : trendData) {
            Integer year = row[0] != null ? ((Number) row[0]).intValue() : 0;
            Integer month = row[1] != null ? ((Number) row[1]).intValue() : 0;
            Long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Double amount = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            monthlyTrends.add(new MonthlyClaimTrendDTO(year, month, count, amount));
        }
        dashboard.setMonthlyTrends(monthlyTrends);

        // 6. Top 10 claims
        List<TopClaimDTO> topClaims = new ArrayList<>();
        List<Object[]> topClaimsData = claimRepository.getTopClaims();
        int limit = Math.min(10, topClaimsData.size());

        for (int i = 0; i < limit; i++) {
            Object[] row = topClaimsData.get(i);
            String claimNumber = row[0] != null ? row[0].toString() : "N/A";
            String policyNumber = row[1] != null ? row[1].toString() : "N/A";
            Double amount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            String status = row[3] != null ? row[3].toString() : "UNKNOWN";
            Date claimDate = row[4] != null ? (Date) row[4] : new Date();

            topClaims.add(new TopClaimDTO(claimNumber, policyNumber, amount, status, claimDate));
        }
        dashboard.setTopClaims(topClaims);

        System.out.println("✅ Dashboard data compiled successfully");
        return dashboard;
    }
}