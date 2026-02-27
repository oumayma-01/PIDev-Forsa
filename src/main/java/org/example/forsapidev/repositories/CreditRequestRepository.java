package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRequestRepository extends JpaRepository<CreditRequest, Long> {
}

