package com.hashtag.ngo.example.bank.entity;

/**
 * Types de comptes bancaires proposés par l'application.
 *
 * <p>Ces constantes n'ont pas vocation à être persistées telles quelles :
 * l'héritage JPA de {@link Account} distingue déjà les sous-types via sa
 * colonne discriminante ({@code @DiscriminatorValue} sur {@link CheckingAccount}
 * et {@link SavingsAccount}). Cet enum sert de vocabulaire métier partagé,
 * notamment pour les couches bean/API qui seront ajoutées ultérieurement.</p>
 */
public enum AccountType {

    /** Compte courant (autorise un découvert dans la limite d'un plafond). */
    COURANT,

    /** Compte épargne (rémunéré, sans découvert autorisé). */
    EPARGNE
}
