package org.example.forsapidev.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIScoreSummaryDto {
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Integer score;            // 0-1000
    private String scoreLevel;        // VERY_LOW..PREMIUM
    private Double creditThreshold;   // TND, null if no score
    private Boolean hasActiveCredit;
    private String lastCalculatedAt;  // ISO string
    private Boolean stegBoosterActive;
    private String stegBoosterExpiry; // ISO string, null if inactive
    private Boolean sonedeBoosterActive;
    private String sonedeBoosterExpiry;
}
