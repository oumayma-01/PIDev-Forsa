package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;
import org.example.forsapidev.entities.PartnershipManagement.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IPartnerTransactionService {
    PartnerTransaction createTransaction(Long clientId, String qrSessionId, Integer durationMonths);
    PartnerTransaction confirmTransaction(Long transactionId);
    PartnerTransaction cancelTransaction(Long transactionId, String reason);
    PartnerTransaction getTransactionById(Long id);
    List<PartnerTransaction> getClientTransactions(Long clientId);
    List<PartnerTransaction> getPartnerTransactions(Long partnerId);
    List<PartnerTransaction> getTransactionsByStatus(TransactionStatus status);
    List<PartnerTransaction> getPartnerTransactionsInPeriod(Long partnerId, LocalDateTime start, LocalDateTime end);
    Double calculateMonthlyPayment(Double amount, Integer months, Double rate);
    void processPartnerPayment(Long transactionId);
}