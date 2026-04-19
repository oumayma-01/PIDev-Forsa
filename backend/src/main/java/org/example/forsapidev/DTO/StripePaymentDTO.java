package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentDTO {
    private Long amount; // Amount in cents
    private String currency; // e.g., "usd", "eur", "tnd"
    private String productName;
    private String successUrl;
    private String cancelUrl;
}
