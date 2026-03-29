package org.example.forsapidev.entities.InsuranceManagement;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "insurance_claim")
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String claimNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date claimDate;

    @Temporal(TemporalType.DATE)
    private Date incidentDate;

    private BigDecimal claimAmount;

    private BigDecimal approvedAmount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private BigDecimal indemnificationPaid;

    // Relationship: Many Claims belong to One Policy
    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = true)    // join w policy claim !!!!!!!!!!!
    @JsonBackReference // Jackson will stop here and NOT go back to the policy
    private InsurancePolicy insurancePolicy;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public Date getClaimDate() { return claimDate; }
    public void setClaimDate(Date claimDate) { this.claimDate = claimDate; }

    public Date getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Date incidentDate) { this.incidentDate = incidentDate; }

    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal claimAmount) { this.claimAmount = claimAmount; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public BigDecimal getIndemnificationPaid() { return indemnificationPaid; }
    public void setIndemnificationPaid(BigDecimal indemnificationPaid) { this.indemnificationPaid = indemnificationPaid; }

    public InsurancePolicy getInsurancePolicy() { return insurancePolicy; }
    public void setInsurancePolicy(InsurancePolicy insurancePolicy) { this.insurancePolicy = insurancePolicy; }
}