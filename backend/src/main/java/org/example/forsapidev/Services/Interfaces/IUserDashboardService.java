package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.payload.response.UserDashboardOverviewDTO;

public interface IUserDashboardService {

    UserDashboardOverviewDTO getOverviewStats();

}