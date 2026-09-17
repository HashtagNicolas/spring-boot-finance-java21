package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;

/**
 * Corps de la requête de création d'un compte.
 *
 * <p>{@code overdraftLimit} n'a de sens que pour un compte courant
 * ({@code type = COURANT}) et {@code interestRate} que pour un compte
 * épargne ({@code type = EPARGNE}) : le champ non pertinent pour le type
 * choisi est simplement ignoré.</p>
 */
public record AccountRequest(
        String owner,
        AccountType type,
        BigDecimal initialBalance,
        BigDecimal overdraftLimit,
        BigDecimal interestRate
) {
}
