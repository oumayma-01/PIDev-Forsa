package org.example.forsapidev.DTO;

public class MonthlyClaimTrendDTO {
    private Integer year;
    private Integer month;
    private Long count;
    private Double totalAmount;

    public MonthlyClaimTrendDTO(Integer year, Integer month, Long count, Double totalAmount) {
        this.year = year;
        this.month = month;
        this.count = count;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}