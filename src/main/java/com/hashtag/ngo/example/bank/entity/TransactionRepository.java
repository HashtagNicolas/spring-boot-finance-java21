package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données pour {@link Transaction}.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
