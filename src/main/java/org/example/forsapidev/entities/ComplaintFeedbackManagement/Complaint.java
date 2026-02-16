package org.example.forsapidev.entities.ComplaintFeedbackManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.User;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "complaint")
@Getter
@Setter
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

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "complaint")
    private Set<Response> responses ;

    @OneToOne(mappedBy = "complaint")
    private Feedback feedback;

    public void setId(Long id) {
    }
}
