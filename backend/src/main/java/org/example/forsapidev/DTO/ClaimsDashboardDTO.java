package org.example.forsapidev.DTO;

import java.util.List;

public class ClaimsDashboardDTO {
    private Long totalClaims;
    private Double totalClaimAmount;
    private Double averageClaimAmount;
    private Double approvalRate;

    private List<ClaimsByStatusDTO> claimsByStatus = new java.util.ArrayList<>();
    private List<ClaimsByTypeDTO> claimsByType = new java.util.ArrayList<>();
    private List<MonthlyClaimTrendDTO> monthlyTrends = new java.util.ArrayList<>();
    private List<TopClaimDTO> topClaims = new java.util.ArrayList<>();

    // Constructor
    public ClaimsDashboardDTO() {}

    // Getters and Setters
    public Long getTotalClaims() { return totalClaims; }
    public void setTotalClaims(Long totalClaims) { this.totalClaims = totalClaims; }

    public Double getTotalClaimAmount() { return totalClaimAmount; }
    public void setTotalClaimAmount(Double totalClaimAmount) { this.totalClaimAmount = totalClaimAmount; }

    public Double getAverageClaimAmount() { return averageClaimAmount; }
    public void setAverageClaimAmount(Double averageClaimAmount) { this.averageClaimAmount = averageClaimAmount; }

    public Double getApprovalRate() { return approvalRate; }
    public void setApprovalRate(Double approvalRate) { this.approvalRate = approvalRate; }

    public List<ClaimsByStatusDTO> getClaimsByStatus() { return claimsByStatus; }
    public void setClaimsByStatus(List<ClaimsByStatusDTO> claimsByStatus) { this.claimsByStatus = claimsByStatus; }

    public List<ClaimsByTypeDTO> getClaimsByType() { return claimsByType; }
    public void setClaimsByType(List<ClaimsByTypeDTO> claimsByType) { this.claimsByType = claimsByType; }

    public List<MonthlyClaimTrendDTO> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyClaimTrendDTO> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public List<TopClaimDTO> getTopClaims() { return topClaims; }
    public void setTopClaims(List<TopClaimDTO> topClaims) { this.topClaims = topClaims; }
}