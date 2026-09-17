package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représentation exposée par l'API d'un compte, quel que soit son type.
 *
 * <p>Seul le champ pertinent pour le {@code type} du compte est renseigné :
 * {@code overdraftLimit} pour un compte courant, {@code interestRate} pour
 * un compte épargne ; l'autre reste {@code null}.</p>
 */
public record AccountResponse(
        Long id,
        String owner,
        AccountType type,
        BigDecimal balance,
        LocalDateTime createdAt,
        BigDecimal overdraftLimit,
        BigDecimal interestRate
) {
}
