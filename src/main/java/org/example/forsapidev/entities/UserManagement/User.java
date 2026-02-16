package org.example.forsapidev.entities.UserManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;

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


    // Relationship: Many Policies belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)  // null for test purposes
    @JsonIgnoreProperties({"insurancePolicies", "password", "passwordHash"})
    private User user;


}