package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.UserManagement.FaceBiometricProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceBiometricProfileRepository extends JpaRepository<FaceBiometricProfile, Long> {
    Optional<FaceBiometricProfile> findByUserId(Long userId);
}
