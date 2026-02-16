package org.example.forsapidev.services;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.repositories.CreditRequestRepository;
import org.example.forsapidev.repositories.RepaymentScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CreditRequestService {

    private final CreditRequestRepository creditRequestRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final InterestRateEngineService interestRateEngineService;

    public CreditRequestService(CreditRequestRepository creditRequestRepository,
                                RepaymentScheduleRepository repaymentScheduleRepository,
                                InterestRateEngineService interestRateEngineService) {
        this.creditRequestRepository = creditRequestRepository;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.interestRateEngineService = interestRateEngineService;
    }

    // Create a credit request and generate repayment schedules automatically
    @Transactional
    public CreditRequest createCreditRequest(CreditRequest request) {
        if (request.getStatus() == null) request.setStatus(CreditStatus.SUBMITTED);
        if (request.getRequestDate() == null) request.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        if (request.getInterestRate() == null || request.getInterestRate() <= 0) {
            BigDecimal finalRatePercent = interestRateEngineService.computeAnnualRatePercent(
                    request.getRequestDate(), request.getDurationMonths(), null, null);
            request.setInterestRate(finalRatePercent.doubleValue());
        }
        return creditRequestRepository.save(request);
    }

    public Optional<CreditRequest> getById(Long id) { return creditRequestRepository.findById(id); }
    public List<CreditRequest> getAll() { return creditRequestRepository.findAll(); }

    @Transactional
    public CreditRequest updateCredit(Long id, CreditRequest update) {
        CreditRequest existing = creditRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Credit not found"));
        existing.setInterestRate(update.getInterestRate());
        existing.setDurationMonths(update.getDurationMonths());
        existing.setStatus(update.getStatus());
        existing.setAgentId(update.getAgentId());
        return creditRequestRepository.save(existing);
    }

    @Transactional
    public void deleteCredit(Long id) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByCreditRequestId(id);
        if (schedules != null && !schedules.isEmpty()) repaymentScheduleRepository.deleteAll(schedules);
        creditRequestRepository.deleteById(id);
    }

    // New: validate (approve) a credit and generate schedules
    @Transactional
    public CreditRequest validateCredit(Long id) {
        CreditRequest credit = creditRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Credit not found"));
        // set status to APPROVED
        credit.setStatus(CreditStatus.APPROVED);
        if (credit.getRequestDate() == null) credit.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        if (credit.getInterestRate() == null || credit.getInterestRate() <= 0) {
            BigDecimal finalRatePercent = interestRateEngineService.computeAnnualRatePercent(
                    credit.getRequestDate(), credit.getDurationMonths(), null, null);
            credit.setInterestRate(finalRatePercent.doubleValue());
        }
        CreditRequest saved = creditRequestRepository.save(credit);
        generateRepaymentScheduleFixedPrincipal(saved);
        return saved;
    }

    @Transactional
    public List<RepaymentSchedule> generateRepaymentScheduleFixedPrincipal(CreditRequest credit) {
        if (credit.getDurationMonths() == null || credit.getDurationMonths() <= 0) return new ArrayList<>();
        BigDecimal principalTotal = credit.getAmountRequested();
        int months = credit.getDurationMonths();
        BigDecimal annualRatePercent = BigDecimal.valueOf(credit.getInterestRate());
        BigDecimal monthlyInterest = interestRateEngineService.computeMonthlyInterest(principalTotal, annualRatePercent);
        BigDecimal principalMonthly = interestRateEngineService.computeMonthlyPrincipal(principalTotal, months);

        List<RepaymentSchedule> schedules = new ArrayList<>();
        BigDecimal remaining = principalTotal;
        java.time.LocalDate start = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate();

        for (int i = 1; i <= months; i++) {
            BigDecimal principalPart = (i == months) ? remaining : principalMonthly;
            BigDecimal totalAmount = principalPart.add(monthlyInterest).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newRemaining = remaining.subtract(principalPart).setScale(2, RoundingMode.HALF_UP);

            RepaymentSchedule sched = new RepaymentSchedule();
            sched.setDueDate(start.plusMonths(i));
            sched.setPrincipalPart(principalPart);
            sched.setInterestPart(monthlyInterest);
            sched.setTotalAmount(totalAmount);
            sched.setRemainingBalance(newRemaining.max(BigDecimal.ZERO));
            sched.setStatus(RepaymentStatus.PENDING);
            sched.setCreditRequest(credit);
            schedules.add(sched);
            remaining = newRemaining;
        }
        return repaymentScheduleRepository.saveAll(schedules);
    }

    @Transactional
    public void onRepaymentPaid(RepaymentSchedule schedule) {
        Long creditId = schedule.getCreditRequest().getId();
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByCreditRequestIdOrderByDueDateAsc(creditId);
        BigDecimal remainingTotal = BigDecimal.ZERO;
        boolean anyPending = false;
        for (RepaymentSchedule s : schedules) {
            if (s.getStatus() != RepaymentStatus.PAID) {
                anyPending = true;
                remainingTotal = remainingTotal.add(s.getRemainingBalance() == null ? BigDecimal.ZERO : s.getRemainingBalance());
            }
        }
        CreditRequest credit = creditRequestRepository.findById(creditId).orElseThrow();
        if (!anyPending) credit.setStatus(CreditStatus.REPAID); else credit.setStatus(CreditStatus.ACTIVE);
        creditRequestRepository.save(credit);
    }
}
