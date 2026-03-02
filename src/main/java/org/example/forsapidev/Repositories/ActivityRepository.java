package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByWallet_Id(Long walletId);
}