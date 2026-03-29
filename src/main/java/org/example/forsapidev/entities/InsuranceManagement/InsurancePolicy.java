package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.example.forsapidev.entities.UserManagement.User; // Import User entity
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

    private String policyType;

    private BigDecimal premiumAmount;

    private BigDecimal coverageLimit;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Temporal(TemporalType.DATE)
    private Date nextPremiumDueDate;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    // NEW ACTUARIAL FIELDS
    private BigDecimal purePremium;           // Prime Pure
    private BigDecimal inventoryPremium;      // Prime Inventaire
    private BigDecimal commercialPremium;     // Prime Commerciale
    private BigDecimal finalPremium;          // Prime Finale (total)

    private Double riskScore;                 // Client risk score
    private String riskCategory;              // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Double riskCoefficient;           // Risk multiplier

    private String paymentFrequency;          // MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    private BigDecimal periodicPaymentAmount; // Amount per payment period
    private Integer numberOfPayments;         // Total number of payments

    private Double effectiveAnnualRate;       // Interest rate used

    @Column(columnDefinition = "TEXT")
    private String calculationNotes;          // Actuarial calculation details

    // Relationship: Many Policies belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)   // null for test purposes
    private User user;

    // Relationship: Many Policies belong to One Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    @JsonBackReference // This prevents the loop back to InsuranceProduct
    private InsuranceProduct insuranceProduct;

    // Relationship: One Policy has Many Premium Payments
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonManagedReference // Jackson will follow this link and serialize the claims
    private Set<PremiumPayment> premiumPayments;

    // Relationship: One Policy has Many Claims
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonManagedReference // Jackson will follow this link and serialize the claims
    private Set<InsuranceClaim> claims;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public InsuranceProduct getInsuranceProduct() { return insuranceProduct; }
    public void setInsuranceProduct(InsuranceProduct insuranceProduct) { this.insuranceProduct = insuranceProduct; }

    public Set<PremiumPayment> getPremiumPayments() { return premiumPayments; }
    public void setPremiumPayments(Set<PremiumPayment> premiumPayments) { this.premiumPayments = premiumPayments; }

    public Set<InsuranceClaim> getClaims() { return claims; }
    public void setClaims(Set<InsuranceClaim> claims) { this.claims = claims; }

    // Getters and Setters for actuarial fields

    public String getCalculationNotes() {
        return calculationNotes;
    }

    public void setCalculationNotes(String calculationNotes) {
        this.calculationNotes = calculationNotes;
    }

    public Double getEffectiveAnnualRate() {
        return effectiveAnnualRate;
    }

    public void setEffectiveAnnualRate(Double effectiveAnnualRate) {
        this.effectiveAnnualRate = effectiveAnnualRate;
    }

    public Integer getNumberOfPayments() {
        return numberOfPayments;
    }

    public void setNumberOfPayments(Integer numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }

    public BigDecimal getPeriodicPaymentAmount() {
        return periodicPaymentAmount;
    }

    public void setPeriodicPaymentAmount(BigDecimal periodicPaymentAmount) {
        this.periodicPaymentAmount = periodicPaymentAmount;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public Double getRiskCoefficient() {
        return riskCoefficient;
    }

    public void setRiskCoefficient(Double riskCoefficient) {
        this.riskCoefficient = riskCoefficient;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public BigDecimal getFinalPremium() {
        return finalPremium;
    }

    public void setFinalPremium(BigDecimal finalPremium) {
        this.finalPremium = finalPremium;
    }

    public BigDecimal getCommercialPremium() {
        return commercialPremium;
    }

    public void setCommercialPremium(BigDecimal commercialPremium) {
        this.commercialPremium = commercialPremium;
    }

    public BigDecimal getInventoryPremium() {
        return inventoryPremium;
    }

    public void setInventoryPremium(BigDecimal inventoryPremium) {
        this.inventoryPremium = inventoryPremium;
    }

    public BigDecimal getPurePremium() {
        return purePremium;
    }

    public void setPurePremium(BigDecimal purePremium) {
        this.purePremium = purePremium;
    }
}