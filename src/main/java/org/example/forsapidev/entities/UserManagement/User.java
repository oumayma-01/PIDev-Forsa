package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String passwordHash;

    private String email;

    private Boolean isActive;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}