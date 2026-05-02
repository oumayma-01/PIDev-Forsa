package org.example.forsapidev.entities.ScoringManagement;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreFeatures {
    private Long clientId;
    private Double avgMonthlyIncome;   // F1 – average deposits over 6 months / 6
    private Double incomeStability;    // F2 – coefficient of variation (stddev/mean); 0=stable, 1=very unstable
    private Double savingsRate;        // F3 – (deposits - withdrawals) / deposits over 6 months
    private Double currentBalance;     // F4 – wallet balance right now
    private Double accountActivity;   // F5 – tx count / days since account creation
    private Double repaymentHistory;  // F6 – repaid / (repaid + defaulted); 0.5 = no history
    private Boolean hasActiveCredit;  // F7 – true if there is an ACTIVE credit right now
    private Double accountAgeMonths;  // F8 – months since user.createdAt
}
