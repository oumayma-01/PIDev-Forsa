package org.example.forsapidev.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Priority;

@Getter
@AllArgsConstructor
public class PriorityStatsDTO {
    private Priority priority;
    private Long count;
}