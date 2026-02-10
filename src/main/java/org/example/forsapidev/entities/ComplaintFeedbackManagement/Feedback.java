package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "feedback")
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
}