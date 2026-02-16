package org.example.forsapidev.repositories;

import org.example.forsapidev.entities.CreditManagement.InflationRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InflationRateRepository extends JpaRepository<InflationRate, Long> {
    Optional<InflationRate> findByYear(Integer year);
}

