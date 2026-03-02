package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.ISmsNotificationService;
import org.example.forsapidev.entities.PartnershipManagement.Partner;
import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsNotificationService implements ISmsNotificationService {

    private final PartnerRepository partnerRepository;

    @Value("${twilio.enabled:false}")
    private Boolean twilioEnabled;

    @Override
    public void sendTransactionConfirmation(PartnerTransaction transaction) {
        Partner partner = partnerRepository.findById(transaction.getPartnerId())
                .orElse(null);

        if (partner == null) {
            return;
        }

        String message = String.format(
                "Forsa: Ton achat de %.2f TND chez %s est confirmé. " +
                        "Tu rembourseras %d mensualités de %.2f TND.",
                transaction.getAmount(),
                partner.getBusinessName(),
                transaction.getDurationMonths(),
                transaction.getMonthlyPayment()
        );

        log.info("SMS to client {}: {}", transaction.getClientId(), message);
    }

    @Override
    public void sendPaymentReminder(Long clientId, Double amount, String dueDate) {
        String message = String.format(
                "Forsa: Rappel - Ta mensualité de %.2f TND arrive le %s. " +
                        "Assure-toi d'avoir le solde suffisant.",
                amount, dueDate
        );

        log.info("SMS reminder to client {}: {}", clientId, message);
    }

    @Override
    public void sendPartnerPaymentNotification(Long partnerId, Double amount, String reference) {
        Partner partner = partnerRepository.findById(partnerId).orElse(null);

        if (partner == null || partner.getBusinessPhone() == null) {
            return;
        }

        String message = String.format(
                "Forsa: Paiement de %.2f TND reçu. Référence: %s",
                amount, reference
        );

        sendSms(partner.getBusinessPhone(), message);
    }

    @Override
    public void sendQRCodeGenerated(Long partnerId, Double amount) {
        Partner partner = partnerRepository.findById(partnerId).orElse(null);

        if (partner == null) {
            return;
        }

        String message = String.format(
                "Forsa: QR code généré pour %.2f TND. Valide 5 minutes.",
                amount
        );

        log.info("SMS to partner {}: {}", partnerId, message);
    }

    @Override
    public void sendFraudAlert(Long clientId, String reason) {
        String message = String.format(
                "Forsa ALERTE: Transaction suspecte détectée. Raison: %s. " +
                        "Contacte-nous si tu n'as pas fait cette transaction.",
                reason
        );

        log.warn("Fraud alert SMS to client {}: {}", clientId, message);
    }

    @Override
    public Boolean sendSms(String phoneNumber, String message) {
        if (!twilioEnabled) {
            log.info("SMS (simulated) to {}: {}", phoneNumber, message);
            return true;
        }

        log.info("Sending SMS to {}: {}", phoneNumber, message);
        return true;
    }
}