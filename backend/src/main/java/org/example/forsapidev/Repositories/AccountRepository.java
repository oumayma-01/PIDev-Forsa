package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByOwner_Id(Long ownerId);

    List<Account> findByWallet_OwnerId(Long ownerId);
}