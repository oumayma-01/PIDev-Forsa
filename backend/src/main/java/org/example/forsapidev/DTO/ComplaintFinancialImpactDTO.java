package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintFinancialImpactDTO {
    private Long complaintId;
    private Double complaintAmount;
    private String amountSource;
    private String priority;
    private Long daysSinceCreation;
    private Double financialImpactScore;
}

