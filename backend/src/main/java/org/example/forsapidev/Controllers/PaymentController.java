package org.example.forsapidev.Controllers;

import com.stripe.exception.StripeException;
import org.example.forsapidev.DTO.StripePaymentDTO;
import org.example.forsapidev.Services.Interfaces.IStripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") // Adjust as needed
public class PaymentController {

    @Autowired
    private IStripeService stripeService;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody StripePaymentDTO paymentDTO) {
        System.out.println("Received Stripe checkout request: " + paymentDTO);
        try {
            String sessionUrl = stripeService.createCheckoutSession(paymentDTO);
            System.out.println("Generated session URL: " + sessionUrl);
            Map<String, String> response = new HashMap<>();
            response.put("sessionUrl", sessionUrl);
            return response;
        } catch (Exception e) {
            System.err.println("Error creating Stripe session: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @PostMapping("/confirm-payment")
    public Map<String, String> confirmPayment(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        System.out.println("Confirming Stripe payment for session: " + sessionId);
        try {
            stripeService.confirmPayment(sessionId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            System.err.println("Error confirming payment: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
