package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "repayment_schedule")
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dueDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal principalPart;

    @Column(precision = 18, scale = 2)
    private BigDecimal interestPart;

    @Column(precision = 18, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    private RepaymentStatus status = RepaymentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_request_id")
    private CreditRequest creditRequest;

    public RepaymentSchedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPrincipalPart() { return principalPart; }
    public void setPrincipalPart(BigDecimal principalPart) { this.principalPart = principalPart; }
    public BigDecimal getInterestPart() { return interestPart; }
    public void setInterestPart(BigDecimal interestPart) { this.interestPart = interestPart; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public RepaymentStatus getStatus() { return status; }
    public void setStatus(RepaymentStatus status) { this.status = status; }
    public CreditRequest getCreditRequest() { return creditRequest; }
    public void setCreditRequest(CreditRequest creditRequest) { this.creditRequest = creditRequest; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepaymentSchedule that = (RepaymentSchedule) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
