package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.User;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "insurance_policy")
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    // REMOVED: policyType (get from insuranceProduct instead)

    private BigDecimal premiumAmount;  // Periodic payment amount

    private BigDecimal coverageLimit;  // Total coverage for this specific policy

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Temporal(TemporalType.DATE)
    private Date nextPremiumDueDate;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;  // PENDING, ACTIVE, SUSPENDED, EXPIRED, CANCELLED

    // ACTUARIAL CALCULATION RESULTS
    private BigDecimal purePremium;           // Prime Pure (E(N) × E(X))
    private BigDecimal inventoryPremium;      // Prime Inventaire (Pure + Management Fees)
    private BigDecimal commercialPremium;     // Prime Commerciale (Inventory / (1 - α))
    private BigDecimal finalPremium;          // Prime Finale (Total amount to be paid)

    // RISK ASSESSMENT RESULTS
    private Double riskScore;                 // Calculated risk score (0-1)
    private String riskCategory;              // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Double riskCoefficient;           // Risk multiplier applied to premium

    // PAYMENT DETAILS
    private String paymentFrequency;          // MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    private BigDecimal periodicPaymentAmount; // Amount per payment period
    private Integer numberOfPayments;         // Total number of scheduled payments
    private Double effectiveAnnualRate;       // Interest rate used in calculations

    @Column(columnDefinition = "TEXT")
    private String calculationNotes;          // Detailed actuarial calculation notes

    // RELATIONSHIP: Many Policies belong to One User (the client/policyholder)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-policies")
    private User user;

    // RELATIONSHIP: Many Policies belong to One Product (the insurance plan)
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference("product-policies")
    private InsuranceProduct insuranceProduct;

    // RELATIONSHIP: One Policy has Many Premium Payments
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonManagedReference("policy-payments")
    private Set<PremiumPayment> premiumPayments;

    // RELATIONSHIP: One Policy has Many Claims
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonManagedReference("policy-claims")
    private Set<InsuranceClaim> claims;

    // CONSTRUCTORS
    public InsurancePolicy() {}

    // HELPER METHOD: Get policy type from product
    @Transient
    public String getPolicyType() {
        return insuranceProduct != null ? insuranceProduct.getPolicyType() : null;
    }


}