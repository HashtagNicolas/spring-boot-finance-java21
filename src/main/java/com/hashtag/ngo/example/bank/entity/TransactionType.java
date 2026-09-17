package com.hashtag.ngo.example.bank.entity;

/**
 * Nature d'une {@link Transaction} appliquée à un {@link Account}.
 */
public enum TransactionType {

    /** Ajout de fonds sur le compte. */
    DEPOT,

    /** Sortie de fonds du compte. */
    RETRAIT
}
