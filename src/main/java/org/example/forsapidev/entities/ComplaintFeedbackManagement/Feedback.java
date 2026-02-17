package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String satisfactionLevel;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private Boolean isAnonymous;

    @OneToOne
    private Complaint complaint;
}