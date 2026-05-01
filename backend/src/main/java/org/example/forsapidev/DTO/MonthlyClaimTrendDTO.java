package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyClaimTrendDTO {
    private Integer year;
    private Integer month;
    private Long count;
    private Double totalAmount;
}