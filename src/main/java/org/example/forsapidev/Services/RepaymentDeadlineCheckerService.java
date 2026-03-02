package org.example.forsapidev.Services;

import org.example.forsapidev.Repositories.RepaymentScheduleRepository;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.entities.CreditManagement.RepaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service planifié qui vérifie les échéances et marque celles en retard
 * en utilisant le champ `dueDate` comparé à la date réelle (LocalDate.now()).
 */
@Service
public class RepaymentDeadlineCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentDeadlineCheckerService.class);

    private final RepaymentScheduleRepository repaymentScheduleRepository;

    public RepaymentDeadlineCheckerService(RepaymentScheduleRepository repaymentScheduleRepository) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
    }

    /**
     * Exécuté toutes les heures : marque les échéances dont la date d'échéance est passée
     * (dueDate < today) et qui ne sont pas payées. Les échéances seront marquées LATE.
     */
    @Scheduled(cron = "0 0 * * * *") // chaque heure à la minute 0
    @Transactional
    public void markOverdueSchedulesAsLate() {
        LocalDate today = LocalDate.now();
        logger.info("Vérification des échéances en retard à la date {}", today);

        // Récupère toutes les échéances avec dueDate < today et status != PAID
        List<RepaymentSchedule> overdue = repaymentScheduleRepository.findByDueDateBeforeAndStatusNot(today, RepaymentStatus.PAID);

        if (overdue == null || overdue.isEmpty()) {
            logger.info("Aucune échéance en retard trouvée");
            return;
        }

        int marked = 0;
        for (RepaymentSchedule schedule : overdue) {
            if (schedule.getStatus() == null || schedule.getStatus() == RepaymentStatus.PENDING) {
                schedule.setStatus(RepaymentStatus.LATE);
                repaymentScheduleRepository.save(schedule);
                marked++;
                logger.warn("Échéance ID={} marquée LATE (dueDate={})", schedule.getId(), schedule.getDueDate());
            }
        }

        logger.info("Nombre d'échéances marquées LATE : {}", marked);
    }
}

