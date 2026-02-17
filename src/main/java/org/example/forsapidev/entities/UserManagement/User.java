package org.example.forsapidev.entities.UserManagement;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;

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

    // Relationship: Many Policies belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)  // null for test purposes
    @JsonIgnoreProperties({"insurancePolicies", "password", "passwordHash"})
    private User user;


}