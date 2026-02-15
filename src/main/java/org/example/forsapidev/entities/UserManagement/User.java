package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;

import java.util.Date;
import java.util.List;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<InsurancePolicy> insurancePolicies;
}