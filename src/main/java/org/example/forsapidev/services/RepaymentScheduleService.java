package org.example.forsapidev.services;

import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.repositories.RepaymentScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RepaymentScheduleService {

    private final RepaymentScheduleRepository repository;
    private final CreditRequestService creditRequestService;

    @Autowired
    public RepaymentScheduleService(RepaymentScheduleRepository repository, CreditRequestService creditRequestService) {
        this.repository = repository;
        this.creditRequestService = creditRequestService;
    }

    public List<RepaymentSchedule> findAll() { return repository.findAll(); }
    public Optional<RepaymentSchedule> findById(Long id) { return repository.findById(id); }
    public List<RepaymentSchedule> findByCreditRequestId(Long creditRequestId) { return repository.findByCreditRequestIdOrderByDueDateAsc(creditRequestId); }

    public RepaymentSchedule createInternal(RepaymentSchedule repaymentSchedule) {
        repaymentSchedule.setId(null);
        return repository.save(repaymentSchedule);
    }

    public Optional<RepaymentSchedule> markAsPaid(Long id, BigDecimal amountPaid) {
        return repository.findById(id).map(existing -> {
            BigDecimal payAmount = amountPaid == null ? existing.getTotalAmount() : amountPaid;
            if (payAmount.compareTo(existing.getRemainingBalance()) >= 0) {
                existing.setRemainingBalance(BigDecimal.ZERO);
                existing.setStatus(RepaymentStatus.PAID);
            } else {
                existing.setRemainingBalance(existing.getRemainingBalance().subtract(payAmount));
            }
            RepaymentSchedule saved = repository.save(existing);
            if (saved.getStatus() == RepaymentStatus.PAID) { creditRequestService.onRepaymentPaid(saved); }
            return saved;
        });
    }

    public Optional<RepaymentSchedule> update(Long id, RepaymentSchedule updated) {
        return repository.findById(id).map(existing -> {
            existing.setDueDate(updated.getDueDate());
            existing.setTotalAmount(updated.getTotalAmount());
            existing.setPrincipalPart(updated.getPrincipalPart());
            existing.setInterestPart(updated.getInterestPart());
            existing.setRemainingBalance(updated.getRemainingBalance());
            existing.setStatus(updated.getStatus());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.findById(id).map(r -> { repository.deleteById(id); return true; }).orElse(false);
    }

    public List<RepaymentSchedule> getSchedulesForCredit(Long creditId) { return repository.findByCreditRequestIdOrderByDueDateAsc(creditId); }
    public RepaymentSchedule getById(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Schedule not found")); }

    @Transactional
    public RepaymentSchedule markAsPaid(Long scheduleId) {
        RepaymentSchedule schedule = getById(scheduleId);
        if (schedule.getStatus() == RepaymentStatus.PAID) { return schedule; }
        schedule.setStatus(RepaymentStatus.PAID);
        RepaymentSchedule saved = repository.save(schedule);
        creditRequestService.onRepaymentPaid(saved);
        return saved;
    }
}

