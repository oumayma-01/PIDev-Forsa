package org.example.forsapidev.Services.Interfaces;

import com.stripe.exception.StripeException;
import org.example.forsapidev.DTO.StripePaymentDTO;

import java.util.Map;

public interface IStripeService {
    String createCheckoutSession(StripePaymentDTO paymentDTO) throws StripeException;
}
