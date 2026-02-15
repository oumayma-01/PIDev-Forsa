package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
}