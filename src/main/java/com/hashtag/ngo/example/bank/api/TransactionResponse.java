package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représentation exposée par l'API d'une transaction.
 */
public record TransactionResponse(Long id, TransactionType type, BigDecimal amount, LocalDateTime timestamp, Long accountId) {
}
