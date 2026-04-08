package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDashboardOverviewDTO {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long newUsersLast30Days;
    private Double activationRate;

    private Long totalClients;
    private Long totalAgents;
    private Long totalAdmins;
}