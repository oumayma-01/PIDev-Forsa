package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.CreditPayment;
import org.example.forsapidev.entities.AIScoreManagement.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CreditPaymentRepository extends JpaRepository<CreditPayment, Long> {
    List<CreditPayment> findByCreditApplicationIdOrderByInstallmentNumber(Long creditId);
    List<CreditPayment> findByDueDateBeforeAndStatus(LocalDate date, PaymentStatus status);
}