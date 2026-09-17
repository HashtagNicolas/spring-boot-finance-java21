package com.hashtag.ngo.example.bank.entity;

/**
 * Levée lorsqu'aucun {@link Account} ne correspond à l'identifiant demandé.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
