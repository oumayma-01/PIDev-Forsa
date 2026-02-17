package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.example.forsapidev.entities.UserManagement.User; // Import User entity
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

    // Relationship: Many Policies belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)   // null for test purposes
    private User user;

    // Relationship: Many Policies belong to One Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("policies")   //to prevent loops oin json results !!!!!!!!!!!
    private InsuranceProduct insuranceProduct;

    // Relationship: One Policy has Many Premium Payments
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("insurancePolicy")    //to prevent loops oin json results !!!!!!!!!!!
    private List<PremiumPayment> premiumPayments;

    // Relationship: One Policy has Many Claims
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("insurancePolicy")  //to prevent loops oin json results !!!!!!!!!!!
    private List<InsuranceClaim> claims;

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

    public List<PremiumPayment> getPremiumPayments() { return premiumPayments; }
    public void setPremiumPayments(List<PremiumPayment> premiumPayments) { this.premiumPayments = premiumPayments; }

    public List<InsuranceClaim> getClaims() { return claims; }
    public void setClaims(List<InsuranceClaim> claims) { this.claims = claims; }
}