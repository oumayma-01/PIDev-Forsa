package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPopularityDTO {
    private String productName;
    private long policyCount;
    private double totalRevenue;
}
