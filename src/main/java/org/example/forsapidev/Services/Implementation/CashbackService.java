package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Services.Interfaces.ICashbackService;
import org.example.forsapidev.entities.PartnershipManagement.Cashback;
import org.example.forsapidev.entities.PartnershipManagement.CashbackStatus;
import org.example.forsapidev.Repositories.PartnershipManagement.CashbackRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CashbackService implements ICashbackService {

    private final CashbackRepository cashbackRepository;

    private static final Double CASHBACK_PERCENTAGE = 0.02;

    @Override
    @Transactional
    public Cashback createCashback(Long clientId, Long transactionId, Double transactionAmount) {
        log.info("Creating cashback for client: {} transaction: {}", clientId, transactionId);

        Double cashbackAmount = calculateCashbackAmount(transactionAmount);

        Cashback cashback = Cashback.builder()
                .clientId(clientId)
                .transactionId(transactionId)
                .amount(cashbackAmount)
                .percentage(CASHBACK_PERCENTAGE)
                .status(CashbackStatus.AVAILABLE)
                .build();

        return cashbackRepository.save(cashback);
    }

    @Override
    public List<Cashback> getClientCashback(Long clientId) {
        return cashbackRepository.findByClientIdAndStatusOrderByEarnedAtDesc(
                clientId, CashbackStatus.AVAILABLE
        );
    }

    @Override
    public Double getAvailableCashbackAmount(Long clientId) {
        Double total = cashbackRepository.getTotalAvailableCashback(clientId);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional
    public void useCashback(Long cashbackId, Long transactionId) {
        log.info("Using cashback: {} for transaction: {}", cashbackId, transactionId);

        Cashback cashback = cashbackRepository.findById(cashbackId)
                .orElseThrow(() -> new RuntimeException("Cashback not found: " + cashbackId));

        if (cashback.getStatus() != CashbackStatus.AVAILABLE) {
            throw new RuntimeException("Cashback not available");
        }

        cashback.setStatus(CashbackStatus.USED);
        cashback.setUsedInTransactionId(transactionId);

        cashbackRepository.save(cashback);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireOldCashback() {
        log.info("Expiring old cashback");

        List<Cashback> expired = cashbackRepository.findExpiredCashback();

        expired.forEach(cashback -> {
            cashback.setStatus(CashbackStatus.EXPIRED);
            cashbackRepository.save(cashback);
        });

        log.info("Expired {} cashback entries", expired.size());
    }

    @Override
    public Double calculateCashbackAmount(Double transactionAmount) {
        return Math.round(transactionAmount * CASHBACK_PERCENTAGE * 100.0) / 100.0;
    }
}