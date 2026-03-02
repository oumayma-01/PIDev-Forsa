package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}