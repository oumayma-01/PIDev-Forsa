package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.PaymentStatus;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
    // Find payments with specific status between two dates
    List<PremiumPayment> findByStatusAndDueDateBetween(PaymentStatus status, Date startDate, Date endDate);

    // Find payments with specific status before a date
    List<PremiumPayment> findByStatusAndDueDateBefore(PaymentStatus status, Date date);

}

