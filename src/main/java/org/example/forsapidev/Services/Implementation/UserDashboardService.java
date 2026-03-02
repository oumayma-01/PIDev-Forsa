package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IUserDashboardService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.payload.response.UserDashboardOverviewDTO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserDashboardService implements IUserDashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDashboardOverviewDTO getOverviewStats() {

        Long totalUsers = userRepository.count();

        Long activeUsers = userRepository.countByIsActive(true);
        Long inactiveUsers = userRepository.countByIsActive(false);

        // Users created in last 30 days
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        Long newUsersLast30Days = userRepository.countByCreatedAtAfter(thirtyDaysAgo);

        Double activationRate = 0.0;
        if (totalUsers > 0) {
            activationRate = (activeUsers.doubleValue() / totalUsers.doubleValue()) * 100;
        }

        Long totalClients = userRepository.countByRole_Name(ERole.CLIENT);
        Long totalAgents = userRepository.countByRole_Name(ERole.AGENT);
        Long totalAdmins = userRepository.countByRole_Name(ERole.ADMIN);

        return new UserDashboardOverviewDTO(
                totalUsers,
                activeUsers,
                inactiveUsers,
                newUsersLast30Days,
                activationRate,
                totalClients,
                totalAgents,
                totalAdmins
        );
    }
}