package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.example.forsapidev.entities.UserManagement.User;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
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

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }

    public BigDecimal getCoverageLimit() { return coverageLimit; }
    public void setCoverageLimit(BigDecimal coverageLimit) { this.coverageLimit = coverageLimit; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getNextPremiumDueDate() { return nextPremiumDueDate; }
    public void setNextPremiumDueDate(Date nextPremiumDueDate) { this.nextPremiumDueDate = nextPremiumDueDate; }

    public PolicyStatus getStatus() { return status; }
    public void setStatus(PolicyStatus status) { this.status = status; }

    // Actuarial fields
    public BigDecimal getPurePremium() { return purePremium; }
    public void setPurePremium(BigDecimal purePremium) { this.purePremium = purePremium; }

    public BigDecimal getInventoryPremium() { return inventoryPremium; }
    public void setInventoryPremium(BigDecimal inventoryPremium) { this.inventoryPremium = inventoryPremium; }

    public BigDecimal getCommercialPremium() { return commercialPremium; }
    public void setCommercialPremium(BigDecimal commercialPremium) { this.commercialPremium = commercialPremium; }

    public BigDecimal getFinalPremium() { return finalPremium; }
    public void setFinalPremium(BigDecimal finalPremium) { this.finalPremium = finalPremium; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public Double getRiskCoefficient() { return riskCoefficient; }
    public void setRiskCoefficient(Double riskCoefficient) { this.riskCoefficient = riskCoefficient; }

    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public BigDecimal getPeriodicPaymentAmount() { return periodicPaymentAmount; }
    public void setPeriodicPaymentAmount(BigDecimal periodicPaymentAmount) { this.periodicPaymentAmount = periodicPaymentAmount; }

    public Integer getNumberOfPayments() { return numberOfPayments; }
    public void setNumberOfPayments(Integer numberOfPayments) { this.numberOfPayments = numberOfPayments; }

    public Double getEffectiveAnnualRate() { return effectiveAnnualRate; }
    public void setEffectiveAnnualRate(Double effectiveAnnualRate) { this.effectiveAnnualRate = effectiveAnnualRate; }

    public String getCalculationNotes() { return calculationNotes; }
    public void setCalculationNotes(String calculationNotes) { this.calculationNotes = calculationNotes; }

    // Relationships
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public InsuranceProduct getInsuranceProduct() { return insuranceProduct; }
    public void setInsuranceProduct(InsuranceProduct insuranceProduct) { this.insuranceProduct = insuranceProduct; }

    public Set<PremiumPayment> getPremiumPayments() { return premiumPayments; }
    public void setPremiumPayments(Set<PremiumPayment> premiumPayments) { this.premiumPayments = premiumPayments; }

    public Set<InsuranceClaim> getClaims() { return claims; }
    public void setClaims(Set<InsuranceClaim> claims) { this.claims = claims; }
}