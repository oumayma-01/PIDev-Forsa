package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.DTO.RepaymentScheduleDTO;
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
    public ResponseEntity<List<RepaymentScheduleDTO>> getSchedulesForCredit(@PathVariable Long creditId) {
        return ResponseEntity.ok(service.getSchedulesForCreditDtos(creditId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepaymentScheduleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.of(service.findDtoById(id));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<RepaymentScheduleDTO> payInstallment(@PathVariable Long id, @RequestParam(required = false) BigDecimal amount) {
        return service.markAsPaidDto(id, amount).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
