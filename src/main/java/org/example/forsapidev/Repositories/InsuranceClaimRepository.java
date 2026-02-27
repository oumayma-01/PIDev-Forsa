package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
}
