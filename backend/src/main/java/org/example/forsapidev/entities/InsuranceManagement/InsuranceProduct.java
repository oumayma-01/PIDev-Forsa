package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "insurance_product")
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;  // e.g., "Basic Health Plan", "Crop Protection Plus"

    @Column(nullable = false)
    private String policyType;  // HEALTH, LIFE, PROPERTY, ACCIDENT, CROP, LIVESTOCK, BUSINESS

    @Column(columnDefinition = "TEXT")
    private String description;  // Detailed product description

    @Column(nullable = false)
    private BigDecimal premiumAmount;  // Base premium amount (monthly rate)

    @Column(nullable = false)
    private BigDecimal coverageLimit;  // Maximum coverage amount

    @Column(nullable = false)
    private Integer durationMonths;  // Standard duration (6, 12, 24, 36 months)

    private Boolean isActive = true;  // Is this product available for sale?

    // RELATIONSHIP: One Product has Many Policies
    @OneToMany(mappedBy = "insuranceProduct", cascade = CascadeType.ALL)
    @JsonManagedReference("product-policies")
    private Set<InsurancePolicy> policies;

    // CONSTRUCTORS
    public InsuranceProduct() {}

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }

    public BigDecimal getCoverageLimit() { return coverageLimit; }
    public void setCoverageLimit(BigDecimal coverageLimit) { this.coverageLimit = coverageLimit; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Set<InsurancePolicy> getPolicies() { return policies; }
    public void setPolicies(Set<InsurancePolicy> policies) { this.policies = policies; }
}