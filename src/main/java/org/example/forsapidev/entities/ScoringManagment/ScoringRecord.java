package org.example.forsapidev.entities.ScoringManagment;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "scoring_record")
public class ScoringRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer financialScore;

    private String riskLevel;

    private Double repaymentRegularityIndex;

    @Temporal(TemporalType.TIMESTAMP)
    private Date calculatedAt;
}