package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "repayment_schedule")
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dueDate;

    private BigDecimal principalAmount;

    private BigDecimal interestAmount;

    private Boolean isPaid;
}