package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données pour {@link Account} (et ses sous-types
 * {@link CheckingAccount}, {@link SavingsAccount}).
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
}
