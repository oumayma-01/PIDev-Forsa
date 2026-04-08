package org.example.forsapidev.Repositories.PartnershipManagement;

import org.example.forsapidev.entities.PartnershipManagement.PartnerAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerAnalyticsRepository extends JpaRepository<PartnerAnalytics, Long> {

    Optional<PartnerAnalytics> findByPartnerIdAndDate(Long partnerId, LocalDate date);

    List<PartnerAnalytics> findByPartnerIdAndDateBetweenOrderByDateAsc(
            Long partnerId, LocalDate startDate, LocalDate endDate
    );

    @Query("SELECT SUM(a.totalVolume) FROM PartnerAnalytics a " +
            "WHERE a.partnerId = :partnerId AND a.date BETWEEN :start AND :end")
    Double getTotalVolumeInPeriod(@Param("partnerId") Long partnerId,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);
}