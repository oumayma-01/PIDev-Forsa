package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {
}
