package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "complaint")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
