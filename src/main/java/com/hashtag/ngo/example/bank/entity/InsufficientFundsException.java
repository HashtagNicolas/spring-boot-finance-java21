package com.hashtag.ngo.example.bank.entity;

/**
 * Levée lorsqu'un retrait dépasse les fonds disponibles sur un {@link Account}
 * (solde insuffisant sur un compte épargne, ou découvert dépassé sur un
 * compte courant).
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
