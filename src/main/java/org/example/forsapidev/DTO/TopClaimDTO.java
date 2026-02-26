package org.example.forsapidev.DTO;

import java.util.Date;

public class TopClaimDTO {
    private String claimNumber;
    private String policyNumber;
    private Double claimAmount;
    private String status;
    private Date claimDate;

    public TopClaimDTO(String claimNumber, String policyNumber, Double claimAmount,
                       String status, Date claimDate) {
        this.claimNumber = claimNumber;
        this.policyNumber = policyNumber;
        this.claimAmount = claimAmount;
        this.status = status;
        this.claimDate = claimDate;
    }

    // Getters and Setters
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public Double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(Double claimAmount) { this.claimAmount = claimAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getClaimDate() { return claimDate; }
    public void setClaimDate(Date claimDate) { this.claimDate = claimDate; }
}