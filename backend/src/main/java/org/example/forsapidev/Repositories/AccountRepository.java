package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByOwner_Id(Long ownerId);

    List<Account> findByWallet_OwnerId(Long ownerId);

    @Query("SELECT DISTINCT a FROM Account a JOIN FETCH a.wallet w LEFT JOIN FETCH w.transactions WHERE a.id = :id")
    Optional<Account> findByIdWithWalletAndTransactions(@Param("id") Long id);
}
