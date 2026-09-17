package com.hashtag.ngo.example.bank.entity;

/**
 * Levée lorsqu'un montant de dépôt ou de retrait est absent ou n'est pas
 * strictement positif.
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
