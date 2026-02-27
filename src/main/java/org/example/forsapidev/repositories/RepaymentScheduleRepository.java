package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByCreditRequestIdOrderByDueDateAsc(Long creditRequestId);
    List<RepaymentSchedule> findByCreditRequestId(Long creditRequestId);
}

