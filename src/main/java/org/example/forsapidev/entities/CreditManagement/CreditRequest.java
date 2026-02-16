package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "credit_request")
public class CreditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amountRequested;

    private Double interestRate;

    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    private CreditStatus status;

    private LocalDateTime requestDate;

    private Long agentId;

    // Fusion tracking fields (no RepaymentSchedule entity)
    @Column(precision = 18, scale = 2)
    private BigDecimal remainingBalance;

    private Integer paidInstallments;

    // Constructors
    public CreditRequest() {
    }

    public CreditRequest(Long id, BigDecimal amountRequested, Double interestRate, Integer durationMonths, CreditStatus status, LocalDateTime requestDate, Long agentId) {
        this.id = id;
        this.amountRequested = amountRequested;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.status = status;
        this.requestDate = requestDate;
        this.agentId = agentId;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public CreditStatus getStatus() {
        return status;
    }

    public void setStatus(CreditStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Integer getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(Integer paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditRequest that = (CreditRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CreditRequest{" +
                "id=" + id +
                ", amountRequested=" + amountRequested +
                ", interestRate=" + interestRate +
                ", durationMonths=" + durationMonths +
                ", status=" + status +
                ", requestDate=" + requestDate +
                ", agentId=" + agentId +
                ", remainingBalance=" + remainingBalance +
                ", paidInstallments=" + paidInstallments +
                '}';
    }
}
