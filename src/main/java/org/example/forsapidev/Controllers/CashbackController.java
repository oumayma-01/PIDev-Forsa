package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.ICashbackService;
import org.example.forsapidev.entities.PartnershipManagement.Cashback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashback")
@RequiredArgsConstructor
public class CashbackController {

    private final ICashbackService cashbackService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Cashback>> getClientCashback(@PathVariable Long clientId) {
        List<Cashback> cashback = cashbackService.getClientCashback(clientId);
        return ResponseEntity.ok(cashback);
    }

    @GetMapping("/client/{clientId}/balance")
    public ResponseEntity<Double> getAvailableCashback(@PathVariable Long clientId) {
        Double balance = cashbackService.getAvailableCashbackAmount(clientId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<Void> useCashback(@PathVariable Long id, @RequestParam Long transactionId) {
        cashbackService.useCashback(id, transactionId);
        return ResponseEntity.ok().build();
    }
}
