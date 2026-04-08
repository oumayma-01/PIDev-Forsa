package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.PartnershipManagement.Cashback;

import java.util.List;

public interface ICashbackService {
    Cashback createCashback(Long clientId, Long transactionId, Double transactionAmount);
    List<Cashback> getClientCashback(Long clientId);
    Double getAvailableCashbackAmount(Long clientId);
    void useCashback(Long cashbackId, Long transactionId);
    void expireOldCashback();
    Double calculateCashbackAmount(Double transactionAmount);
}