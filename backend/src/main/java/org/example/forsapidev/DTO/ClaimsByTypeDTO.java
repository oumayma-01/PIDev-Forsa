package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimsByTypeDTO {
    private String policyType;
    private Long count;
    private Double totalAmount;
    private Double averageAmount;
}