package org.example.forsapidev.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IPartnerTransactionService;
import org.example.forsapidev.entities.PartnershipManagement.PartnerTransaction;
import org.example.forsapidev.entities.PartnershipManagement.TransactionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/partner-transactions")
@RequiredArgsConstructor
public class PartnerTransactionController {

    private final IPartnerTransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<PartnerTransaction> createTransaction(
            @RequestParam Long clientId,
            @RequestParam String qrSessionId,
            @RequestParam Integer durationMonths) {
        PartnerTransaction transaction = transactionService.createTransaction(clientId, qrSessionId, durationMonths);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PartnerTransaction> confirmTransaction(@PathVariable Long id) {
        PartnerTransaction confirmed = transactionService.confirmTransaction(id);
        return ResponseEntity.ok(confirmed);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PartnerTransaction> cancelTransaction(@PathVariable Long id, @RequestParam String reason) {
        PartnerTransaction cancelled = transactionService.cancelTransaction(id, reason);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerTransaction> getTransaction(@PathVariable Long id) {
        PartnerTransaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PartnerTransaction>> getClientTransactions(@PathVariable Long clientId) {
        List<PartnerTransaction> transactions = transactionService.getClientTransactions(clientId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerTransaction>> getPartnerTransactions(@PathVariable Long partnerId) {
        List<PartnerTransaction> transactions = transactionService.getPartnerTransactions(partnerId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PartnerTransaction>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        List<PartnerTransaction> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }
}