package org.example.forsapidev.Services;

import org.example.forsapidev.entities.CreditManagement.AmortizationType;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.CreditStatus;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.Repositories.CreditRequestRepository;
import org.example.forsapidev.Repositories.RepaymentScheduleRepository;
import org.example.forsapidev.Services.amortization.AmortizationCalculatorService;
import org.example.forsapidev.Services.amortization.AmortizationResult;
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
    private final AmortizationCalculatorService amortizationCalculatorService;

    public CreditRequestService(CreditRequestRepository creditRequestRepository,
                                RepaymentScheduleRepository repaymentScheduleRepository,
                                InterestRateEngineService interestRateEngineService,
                                AmortizationCalculatorService amortizationCalculatorService) {
        this.creditRequestRepository = creditRequestRepository;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.interestRateEngineService = interestRateEngineService;
        this.amortizationCalculatorService = amortizationCalculatorService;
    }

    // Create a credit request and generate repayment schedules automatically
    @Transactional
    public CreditRequest createCreditRequest(CreditRequest request) {
        if (request.getStatus() == null) request.setStatus(CreditStatus.SUBMITTED);
        if (request.getRequestDate() == null) request.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        if (request.getTypeCalcul() == null) request.setTypeCalcul(AmortizationType.AMORTISSEMENT_CONSTANT);
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
        CreditRequest credit = creditRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));

        // Validation du statut
        if (credit.getStatus() == CreditStatus.APPROVED || credit.getStatus() == CreditStatus.ACTIVE) {
            throw new IllegalStateException("Le crédit est déjà validé");
        }

        // set status to APPROVED
        credit.setStatus(CreditStatus.APPROVED);

        // Initialisation des champs si nécessaires
        if (credit.getRequestDate() == null) {
            credit.setRequestDate(ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (credit.getInterestRate() == null || credit.getInterestRate() <= 0) {
            BigDecimal finalRatePercent = interestRateEngineService.computeAnnualRatePercent(
                    credit.getRequestDate(), credit.getDurationMonths(), null, null);
            credit.setInterestRate(finalRatePercent.doubleValue());
        }
        if (credit.getTypeCalcul() == null) {
            credit.setTypeCalcul(AmortizationType.AMORTISSEMENT_CONSTANT);
        }

        CreditRequest saved = creditRequestRepository.save(credit);

        // Génération du tableau d'amortissement selon le type choisi
        generateRepaymentSchedule(saved);

        return saved;
    }

    /**
     * Génère le tableau d'amortissement selon le type de calcul choisi
     * Utilise le pattern Strategy via AmortizationCalculatorService
     */
    @Transactional
    public List<RepaymentSchedule> generateRepaymentSchedule(CreditRequest credit) {
        if (credit.getDurationMonths() == null || credit.getDurationMonths() <= 0) {
            return new ArrayList<>();
        }

        BigDecimal principal = credit.getAmountRequested();
        BigDecimal annualRatePercent = BigDecimal.valueOf(credit.getInterestRate());
        int durationMonths = credit.getDurationMonths();
        AmortizationType type = credit.getTypeCalcul();

        if (type == null) {
            type = AmortizationType.AMORTISSEMENT_CONSTANT;
        }

        // Utilisation du service de calcul avec Strategy Pattern
        AmortizationResult result = amortizationCalculatorService.calculateSchedule(
                type, principal, annualRatePercent, durationMonths);

        // Conversion des périodes en entités RepaymentSchedule
        List<RepaymentSchedule> schedules = new ArrayList<>();
        java.time.LocalDate startDate = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate();

        for (AmortizationResult.MonthlyPeriod period : result.getPeriods()) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setDueDate(startDate.plusMonths(period.getMonthNumber()));
            schedule.setPrincipalPart(period.getPrincipalPayment());
            schedule.setInterestPart(period.getInterestPayment());
            schedule.setTotalAmount(period.getTotalPayment());
            schedule.setRemainingBalance(period.getRemainingBalance());
            schedule.setStatus(RepaymentStatus.PENDING);
            schedule.setCreditRequest(credit);
            schedules.add(schedule);
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
