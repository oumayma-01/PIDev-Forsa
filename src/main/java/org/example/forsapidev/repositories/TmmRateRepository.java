package org.example.forsapidev.repositories;

import org.example.forsapidev.entities.CreditManagement.TmmRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TmmRateRepository extends JpaRepository<TmmRate, Long> {
    Optional<TmmRate> findByYear(Integer year);
}

