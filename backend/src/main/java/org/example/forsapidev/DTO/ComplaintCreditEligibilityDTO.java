package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCreditEligibilityDTO {
    private Long complaintId;
    private Long clientId;
    private Double currentScore;
    private Double requiredScore;
    private Double gap;
    private boolean eligible;
    private boolean fallbackUsed;
    private Double requestedAmount;
    private String recommendation;
}
