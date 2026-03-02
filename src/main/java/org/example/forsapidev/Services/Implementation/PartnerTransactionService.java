package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.*;
import org.example.forsapidev.entities.PartnershipManagement.*;
import org.example.forsapidev.Repositories.PartnershipManagement.PartnerTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerTransactionService implements org.example.forsapidev.Services.Interfaces.IPartnerTransactionService {

    private final PartnerTransactionRepository transactionRepository;
    private final org.example.forsapidev.Services.Interfaces.IQRCodeService qrCodeService;
    private final org.example.forsapidev.Services.Interfaces.IPartnerService partnerService;
    private final org.example.forsapidev.Services.Interfaces.IFraudDetectionService fraudDetectionService;
    private final org.example.forsapidev.Services.Interfaces.ICashbackService cashbackService;
    private final org.example.forsapidev.Services.Interfaces.ISmsNotificationService smsNotificationService;

    @Override
    @Transactional
    public PartnerTransaction createTransaction(Long clientId, String qrSessionId, Integer durationMonths) {
        log.info("Creating transaction for client: {} with QR: {}", clientId, qrSessionId);

        QRCodeSession session = qrCodeService.validateQRCode(qrSessionId);
        Partner partner = partnerService.getPartnerById(session.getPartnerId());

        Double amount = session.getAmount();
        Double commissionRate = partner.getCommissionRate();
        Double commissionAmount = amount * commissionRate;
        Double netAmount = amount - commissionAmount;

        Double interestRate = 0.10;
        Double monthlyPayment = calculateMonthlyPayment(amount, durationMonths, interestRate);
        Double totalRepayment = monthlyPayment * durationMonths;

        PartnerTransaction transaction = PartnerTransaction.builder()
                .clientId(clientId)
                .partnerId(partner.getId())
                .amount(amount)
                .commissionAmount(commissionAmount)
                .netAmountToPartner(netAmount)
                .durationMonths(durationMonths)
                .interestRate(interestRate)
                .monthlyPayment(monthlyPayment)
                .totalRepayment(totalRepayment)
                .qrCodeSessionId(qrSessionId)
                .status(TransactionStatus.PENDING)
                .build();

        PartnerTransaction saved = transactionRepository.save(transaction);

        return saved;
    }

    @Override
    @Transactional
    public PartnerTransaction confirmTransaction(Long transactionId) {
        log.info("Confirming transaction: {}", transactionId);

        PartnerTransaction transaction = getTransactionById(transactionId);

        Boolean isFraud = fraudDetectionService.checkTransaction(transaction);

        if (isFraud) {
            transaction.setStatus(TransactionStatus.FRAUD_BLOCKED);
            transaction.setIsFraudulent(true);
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction blocked due to fraud detection");
        }

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(LocalDateTime.now());

        PartnerTransaction confirmed = transactionRepository.save(transaction);

        processPartnerPayment(transactionId);

        cashbackService.createCashback(
                transaction.getClientId(),
                transaction.getId(),
                transaction.getAmount()
        );

        smsNotificationService.sendTransactionConfirmation(confirmed);

        qrCodeService.markQRCodeAsUsed(transaction.getQrCodeSessionId(), transactionId);

        return confirmed;
    }

    @Override
    @Transactional
    public PartnerTransaction cancelTransaction(Long transactionId, String reason) {
        log.info("Cancelling transaction: {} for reason: {}", transactionId, reason);

        PartnerTransaction transaction = getTransactionById(transactionId);
        transaction.setStatus(TransactionStatus.CANCELLED);

        return transactionRepository.save(transaction);
    }

    @Override
    public PartnerTransaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    @Override
    public List<PartnerTransaction> getClientTransactions(Long clientId) {
        return transactionRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Override
    public List<PartnerTransaction> getPartnerTransactions(Long partnerId) {
        return transactionRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId);
    }

    @Override
    public List<PartnerTransaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    @Override
    public List<PartnerTransaction> getPartnerTransactionsInPeriod(Long partnerId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByPartnerAndDateRange(partnerId, start, end);
    }

    @Override
    public Double calculateMonthlyPayment(Double amount, Integer months, Double rate) {
        if (months == 1) {
            return amount * (1 + rate);
        }

        double monthlyRate = rate / 12;
        double factor = Math.pow(1 + monthlyRate, months);
        return amount * (monthlyRate * factor) / (factor - 1);
    }

    @Override
    @Transactional
    public void processPartnerPayment(Long transactionId) {
        log.info("Processing partner payment for transaction: {}", transactionId);

        PartnerTransaction transaction = getTransactionById(transactionId);

        transaction.setStatus(TransactionStatus.PAID);
        transaction.setPaidToPartnerAt(LocalDateTime.now());
        transaction.setPaymentReference("PAY-" + System.currentTimeMillis());
        transaction.setPaymentMethod("FLOUCI");

        transactionRepository.save(transaction);

        partnerService.updatePartnerStats(
                transaction.getPartnerId(),
                transaction.getAmount()
        );

        smsNotificationService.sendPartnerPaymentNotification(
                transaction.getPartnerId(),
                transaction.getNetAmountToPartner(),
                transaction.getPaymentReference()
        );
    }
}