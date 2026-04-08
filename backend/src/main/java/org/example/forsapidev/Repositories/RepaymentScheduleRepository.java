package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.example.forsapidev.entities.CreditManagement.LineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByCreditRequestIdOrderByDueDateAsc(Long creditRequestId);
    List<RepaymentSchedule> findByCreditRequestId(Long creditRequestId);

    // Récupère toutes les échéances payées pour un utilisateur donné (via CreditRequest -> User)
    @Query("SELECT r FROM RepaymentSchedule r " +
           "WHERE r.creditRequest.user.id = :userId " +
           "AND r.status = :status")
    List<RepaymentSchedule> findPaidByUserId(@Param("userId") Long userId,
                                             @Param("status") RepaymentStatus status);

    // Méthodes pour la gestion des pénalités
    List<RepaymentSchedule> findByDueDateBeforeAndStatusNot(LocalDate dueDate, RepaymentStatus status);
    List<RepaymentSchedule> findByCreditRequestIdAndLineType(Long creditRequestId, LineType lineType);
    List<RepaymentSchedule> findByCreditRequestIdOrderByDueDateDesc(Long creditRequestId);
    long countByLineType(LineType lineType);
}
