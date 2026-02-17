package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "response")
@Getter
@Setter
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String responderRole;

    private String responderName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date responseDate;

    private String responseStatus;

    @ManyToOne
    private Complaint complaint;
}