package org.example.forsapidev.Services.Implementation;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.example.forsapidev.DTO.StripePaymentDTO;
import org.example.forsapidev.Services.Interfaces.IStripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.forsapidev.Repositories.PremiumPaymentRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PolicyStatus;
import java.util.Date;

@Service
public class StripeService implements IStripeService {

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Autowired
    private PremiumPaymentRepository premiumPaymentRepository;

    @Autowired
    private InsurancePolicyRepository insurancePolicyRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public String createCheckoutSession(StripePaymentDTO paymentDTO) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(paymentDTO.getSuccessUrl())
                .setCancelUrl(paymentDTO.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(paymentDTO.getCurrency())
                                                .setUnitAmount(paymentDTO.getAmount())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(paymentDTO.getProductName())
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("paymentId", paymentDTO.getPaymentId() != null ? paymentDTO.getPaymentId().toString() : "")
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    @Override
    public void confirmPayment(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        if ("paid".equals(session.getPaymentStatus())) {
            String paymentIdStr = session.getMetadata().get("paymentId");
            if (paymentIdStr != null && !paymentIdStr.isEmpty()) {
                Long paymentId = Long.parseLong(paymentIdStr);
                PremiumPayment payment = premiumPaymentRepository.findById(paymentId).orElse(null);
                if (payment != null && payment.getStatus() != PaymentStatus.PAID) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidDate(new Date());
                    // Use Stripe's payment intent ID or session ID as transaction ID
                    // Note: PremiumPayment.transactionId is Long in this project, so we might need to hash it or just use a random one as they did in frontend
                    payment.setTransactionId(System.currentTimeMillis()); 
                    premiumPaymentRepository.save(payment);

                    // If this is the first payment of a pending policy, we might want to activate it
                    InsurancePolicy policy = payment.getInsurancePolicy();
                    if (policy != null && policy.getStatus() == PolicyStatus.PENDING) {
                        // Activate policy logic could be here or in a separate service
                        // For now let's just update the payment status as requested
                    }
                }
            }
        }
    }
}
