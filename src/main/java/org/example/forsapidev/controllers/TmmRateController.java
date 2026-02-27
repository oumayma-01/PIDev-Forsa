package org.example.forsapidev.Controllers;

import org.example.forsapidev.entities.CreditManagement.TmmRate;
import org.example.forsapidev.Repositories.TmmRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tmm")
public class TmmRateController {

    private final TmmRateRepository repo;

    public TmmRateController(TmmRateRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<TmmRate>> list() { return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<TmmRate> get(@PathVariable Long id) { return ResponseEntity.of(repo.findById(id)); }

    @PostMapping
    public ResponseEntity<TmmRate> create(@RequestBody TmmRate rate) { return ResponseEntity.ok(repo.save(rate)); }

    @PutMapping("/{id}")
    public ResponseEntity<TmmRate> update(@PathVariable Long id, @RequestBody TmmRate rate) {
        return repo.findById(id).map(existing -> {
            existing.setYear(rate.getYear());
            existing.setPercent(rate.getPercent());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { repo.deleteById(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/year/{year}")
    public ResponseEntity<TmmRate> findByYear(@PathVariable Integer year) {
        return ResponseEntity.of(repo.findByYear(year));
    }
}

