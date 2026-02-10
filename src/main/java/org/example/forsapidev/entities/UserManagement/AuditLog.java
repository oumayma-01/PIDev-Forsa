package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String details;
}