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
        long start = System.currentTimeMillis();
        System.out.println("🔍 Claims Dashboard Compilation Started...");

        // 1. Get total claims and amounts
        long s1 = System.currentTimeMillis();
        Long totalClaims = claimRepository.getTotalClaimsCount();
        Double totalAmount = claimRepository.getTotalClaimAmount();
        System.out.println("⏱️ Step 1 (Totals): " + (System.currentTimeMillis() - s1) + "ms");

        dashboard.setTotalClaims(totalClaims != null ? totalClaims : 0L);
        dashboard.setTotalClaimAmount(totalAmount != null ? totalAmount : 0.0);

        // Calculate average
        if (totalClaims != null && totalClaims > 0 && totalAmount != null) {
            dashboard.setAverageClaimAmount(totalAmount / totalClaims);
        } else {
            dashboard.setAverageClaimAmount(0.0);
        }

        // 2. Calculate approval rate
        long s2 = System.currentTimeMillis();
        Long approvedCount = claimRepository.getApprovedClaimsCount();
        if (totalClaims != null && totalClaims > 0) {
            dashboard.setApprovalRate((approvedCount.doubleValue() / totalClaims) * 100);
        } else {
            dashboard.setApprovalRate(0.0);
        }
        System.out.println("⏱️ Step 2 (Approval): " + (System.currentTimeMillis() - s2) + "ms");

        // 3. Claims by status
        long s3 = System.currentTimeMillis();
        List<ClaimsByStatusDTO> claimsByStatus = new ArrayList<>();
        List<Object[]> statusData = claimRepository.countClaimsByStatus();
        for (Object[] row : statusData) {
            claimsByStatus.add(new ClaimsByStatusDTO(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
            ));
        }
        dashboard.setClaimsByStatus(claimsByStatus);
        System.out.println("⏱️ Step 3 (Status): " + (System.currentTimeMillis() - s3) + "ms");

        // 4. Claims by policy type
        long s4 = System.currentTimeMillis();
        List<ClaimsByTypeDTO> claimsByType = new ArrayList<>();
        List<Object[]> typeData = claimRepository.countClaimsByPolicyType();
        for (Object[] row : typeData) {
            String policyType = row[0] != null ? row[0].toString() : "Unknown";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double total = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double avg = count > 0 ? total / count : 0.0;
            claimsByType.add(new ClaimsByTypeDTO(policyType, count, total, avg));
        }
        dashboard.setClaimsByType(claimsByType);
        System.out.println("⏱️ Step 4 (Types): " + (System.currentTimeMillis() - s4) + "ms");

        // 5. Monthly trends
        long s5 = System.currentTimeMillis();
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
        System.out.println("⏱️ Step 5 (Trends): " + (System.currentTimeMillis() - s5) + "ms");

        // 6. Top 10 claims
        long s6 = System.currentTimeMillis();
        List<TopClaimDTO> topClaims = new ArrayList<>();
        List<Object[]> topClaimsData = claimRepository.getTopClaims();
        int limit = Math.min(10, topClaimsData.size());

        for (int i = 0; i < limit; i++) {
            Object[] row = topClaimsData.get(i);
            Long id = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            String claimNumber = row[1] != null ? row[1].toString() : "N/A";
            String policyNumber = row[2] != null ? row[2].toString() : "N/A";
            Double amount = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            String status = row[4] != null ? row[4].toString() : "UNKNOWN";
            Date claimDate = row[5] != null ? (Date) row[5] : new Date();

            topClaims.add(new TopClaimDTO(id, claimNumber, policyNumber, amount, status, claimDate));
        }
        dashboard.setTopClaims(topClaims);
        System.out.println("⏱️ Step 6 (Top Claims): " + (System.currentTimeMillis() - s6) + "ms");

        System.out.println("✅ Dashboard data compiled successfully in " + (System.currentTimeMillis() - start) + "ms");
        return dashboard;
    }
}