package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFinancialDashboardDTO {
    private FinancialKpiDTO kpis = new FinancialKpiDTO();
    private List<MonthlyPointDTO> monthlyRevenueVsClaims = new ArrayList<>();
    private List<LabelValueDTO> insuranceStatusSplit = new ArrayList<>();
    private List<LabelValueDTO> creditStatusSplit = new ArrayList<>();
    private List<LabelValueDTO> walletFlowSplit = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialKpiDTO {
        private double totalPremiumCollected;
        private double totalClaimsPaid;
        private double lossRatioPct;
        private double claimFrequencyPct;
        private double creditPortfolioOutstanding;
        private double creditApprovalRatePct;
        private double weightedAverageInterestRatePct;
        private double portfolioAtRiskPct;
        private double totalWalletBalances;
        private double netCashFlow;
        private double solvencyMarginPct;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPointDTO {
        private String month;
        private double premiumCollected;
        private double claimsPaid;
        private double creditDisbursed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabelValueDTO {
        private String label;
        private double value;
    }
}
