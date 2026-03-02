package org.example.forsapidev.DTO;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.PriorityLevel;


import java.sql.Date;

public class ComplaintDTO {
    private Long id;
    private String subject;
    private String description;
    private String category;   // Category enum en String
    private String status;
    private Date createdAt;
    private Long userId;
    private PriorityLevel priority;

}
