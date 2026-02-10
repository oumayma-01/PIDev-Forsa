package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String identityNumber;

    private String kycStatus;

    @Column(columnDefinition = "TEXT")
    private String socioEconomicProfile;

    private BigDecimal incomeIndicator;
}