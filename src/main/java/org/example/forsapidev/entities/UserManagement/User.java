package org.example.forsapidev.entities.UserManagement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Complaint;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

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
    @Column(name = "reset_token")
    @JsonIgnore
    private String resetToken;

    @Column()
    @JsonIgnore
    private Date expiryDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne()
    @JoinColumn(name = "Id_Role")
    private Role role;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Complaint> complaints;

    // Relationship: Many Policies belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)  // null for test purposes
    @JsonIgnoreProperties({"insurancePolicies", "password", "passwordHash"})
    private User user;
    public void setExpiryDate(Integer minutes){
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes);
        this.expiryDate = now.getTime();
    }

    public boolean isExpired() {
        if (this.expiryDate == null) {
            return false;
        }
        return new Date().after(this.expiryDate);
    }

}