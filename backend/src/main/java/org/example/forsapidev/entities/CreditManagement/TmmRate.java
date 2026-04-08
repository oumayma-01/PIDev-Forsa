package org.example.forsapidev.entities.CreditManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tmm_rate")
public class TmmRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Integer year; // ex: 2025
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal percent; // ex: 7.49

    public TmmRate() {}
    public TmmRate(Long id, Integer year, BigDecimal percent) { this.id = id; this.year = year; this.percent = percent; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getPercent() { return percent; }
    public void setPercent(BigDecimal percent) { this.percent = percent; }
}

