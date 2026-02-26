package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    // _____________________ Dashboard Queries (JPQL Query) ______________________
    // Count claims by status
    @Query("SELECT c.status, COUNT(c) FROM InsuranceClaim c GROUP BY c.status")
    List<Object[]> countClaimsByStatus();

    // Count claims by policy type with amounts
    @Query("SELECT p.policyType, COUNT(c), SUM(c.claimAmount) " +
            "FROM InsuranceClaim c JOIN c.insurancePolicy p " +
            "GROUP BY p.policyType")
    List<Object[]> countClaimsByPolicyType();

    // Monthly claim trends for last 12 months
    @Query("SELECT YEAR(c.claimDate), MONTH(c.claimDate), COUNT(c), SUM(c.claimAmount) " +
            "FROM InsuranceClaim c " +
            "WHERE c.claimDate >= CURRENT_DATE - 365 " +
            "GROUP BY YEAR(c.claimDate), MONTH(c.claimDate) " +
            "ORDER BY YEAR(c.claimDate), MONTH(c.claimDate)")
    List<Object[]> getMonthlyClaimTrends();

    // Top 10 highest claims
    @Query("SELECT c.claimNumber, p.policyNumber, c.claimAmount, c.status, c.claimDate " +
            "FROM InsuranceClaim c JOIN c.insurancePolicy p " +
            "ORDER BY c.claimAmount DESC")
    List<Object[]> getTopClaims();

    // Total claims count
    @Query("SELECT COUNT(c) FROM InsuranceClaim c")
    Long getTotalClaimsCount();

    // Total claim amount
    @Query("SELECT SUM(c.claimAmount) FROM InsuranceClaim c WHERE c.claimAmount IS NOT NULL")
    Double getTotalClaimAmount();

    // Count approved claims
    @Query("SELECT COUNT(c) FROM InsuranceClaim c WHERE c.status = 'APPROVED' OR c.status = 'PAID'")
    Long getApprovedClaimsCount();

}