package org.example.forsapidev.DTO;

import org.example.forsapidev.entities.CreditManagement.LineType;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de sortie pour RepaymentSchedule.
 */
public class RepaymentScheduleDTO {

    private Long id;
    private LocalDate dueDate;
    private LocalDate paidDate;

    private BigDecimal totalAmount;
    private BigDecimal principalPart;
    private BigDecimal interestPart;
    private BigDecimal remainingBalance;

    private RepaymentStatus status;
    private LineType lineType;

    private CreditRefDTO creditRequest;

    public RepaymentScheduleDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPrincipalPart() {
        return principalPart;
    }

    public void setPrincipalPart(BigDecimal principalPart) {
        this.principalPart = principalPart;
    }

    public BigDecimal getInterestPart() {
        return interestPart;
    }

    public void setInterestPart(BigDecimal interestPart) {
        this.interestPart = interestPart;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public RepaymentStatus getStatus() {
        return status;
    }

    public void setStatus(RepaymentStatus status) {
        this.status = status;
    }

    public LineType getLineType() {
        return lineType;
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public CreditRefDTO getCreditRequest() {
        return creditRequest;
    }

    public void setCreditRequest(CreditRefDTO creditRequest) {
        this.creditRequest = creditRequest;
    }
}
