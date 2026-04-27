package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.BankVault;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankVaultRepository extends JpaRepository<BankVault, Long> {
}