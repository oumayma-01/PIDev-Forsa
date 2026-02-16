package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
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
    @ManyToOne()
    @JoinColumn(name = "Id_Role")
    private Role role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<InsurancePolicy> insurancePolicies;
}