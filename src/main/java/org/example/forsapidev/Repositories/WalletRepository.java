package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}