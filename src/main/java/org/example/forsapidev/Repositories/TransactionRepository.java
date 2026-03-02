package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}