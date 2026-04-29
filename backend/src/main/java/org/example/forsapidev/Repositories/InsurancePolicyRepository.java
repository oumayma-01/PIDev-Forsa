package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {
    
    @Query("SELECT SUM(p.periodicPaymentAmount) FROM InsurancePolicy p WHERE p.status = 'ACTIVE'")
    Double getTotalActivePremiumRevenue();

    @Query("SELECT COUNT(p) FROM InsurancePolicy p WHERE p.status = 'ACTIVE'")
    long countActivePolicies();

    @Query("SELECT COUNT(p) FROM InsurancePolicy p WHERE p.status = 'CANCELLED'")
    long countCanceledPolicies();

    @Query("SELECT COUNT(p) FROM InsurancePolicy p WHERE p.status = 'PENDING'")
    long countPendingPolicies();

    @Query("SELECT p.insuranceProduct.productName, COUNT(p), SUM(p.periodicPaymentAmount) " +
           "FROM InsurancePolicy p " +
           "GROUP BY p.insuranceProduct.productName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getProductPopularityData();
}
