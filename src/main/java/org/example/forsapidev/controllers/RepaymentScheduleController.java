package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.example.forsapidev.Services.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/repayments")
public class RepaymentScheduleController {

    private final RepaymentScheduleService service;

    @Autowired
    public RepaymentScheduleController(RepaymentScheduleService service) { this.service = service; }

    @GetMapping("/credit/{creditId}")
    public ResponseEntity<List<RepaymentSchedule>> getSchedulesForCredit(@PathVariable Long creditId) {
        return ResponseEntity.ok(service.getSchedulesForCredit(creditId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepaymentSchedule> getById(@PathVariable Long id) {
        return ResponseEntity.of(service.findById(id));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<RepaymentSchedule> payInstallment(@PathVariable Long id, @RequestParam(required = false) BigDecimal amount) {
        return service.markAsPaid(id, amount).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
