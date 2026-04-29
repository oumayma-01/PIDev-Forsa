package org.example.forsapidev.DTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopClaimDTO {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private Double claimAmount;
    private String status;
    private Date claimDate;
}