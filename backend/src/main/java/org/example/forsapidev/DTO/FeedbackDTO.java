package org.example.forsapidev.DTO;

import java.sql.Date;

public class FeedbackDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private String satisfactionLevel;
    private Date createdAt;
    private Boolean isAnonymous;
    private Long complaintId;
}

