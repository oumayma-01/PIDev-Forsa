package org.example.forsapidev.controllers;

import org.example.forsapidev.entities.CreditManagement.InflationRate;
import org.example.forsapidev.repositories.InflationRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inflation")
public class InflationRateController {

    private final InflationRateRepository repo;

    public InflationRateController(InflationRateRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<InflationRate>> list() { return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<InflationRate> get(@PathVariable Long id) { return ResponseEntity.of(repo.findById(id)); }

    @PostMapping
    public ResponseEntity<InflationRate> create(@RequestBody InflationRate rate) { return ResponseEntity.ok(repo.save(rate)); }

    @PutMapping("/{id}")
    public ResponseEntity<InflationRate> update(@PathVariable Long id, @RequestBody InflationRate rate) {
        return repo.findById(id).map(existing -> {
            existing.setYear(rate.getYear());
            existing.setPercent(rate.getPercent());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { repo.deleteById(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/year/{year}")
    public ResponseEntity<InflationRate> findByYear(@PathVariable Integer year) {
        return ResponseEntity.of(repo.findByYear(year));
    }
}

