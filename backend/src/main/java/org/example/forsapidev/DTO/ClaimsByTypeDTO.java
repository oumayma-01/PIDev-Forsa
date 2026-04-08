package org.example.forsapidev.DTO;

public class ClaimsByTypeDTO {
    private String policyType;
    private Long count;
    private Double totalAmount;
    private Double averageAmount;

    public ClaimsByTypeDTO(String policyType, Long count, Double totalAmount) {
        this.policyType = policyType;
        this.count = count;
        this.totalAmount = totalAmount;
        this.averageAmount = count > 0 ? totalAmount / count : 0.0;
    }

    // Getters and Setters
    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getAverageAmount() { return averageAmount; }
    public void setAverageAmount(Double averageAmount) { this.averageAmount = averageAmount; }
}