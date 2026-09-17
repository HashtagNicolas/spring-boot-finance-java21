package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;

/**
 * Commande représentant l'intention d'appliquer un dépôt ou un retrait à un
 * compte. Utilisée à la fois par {@link AccountService} (pour muter le
 * solde du compte) et {@link TransactionService} (pour enregistrer
 * l'historique correspondant).
 *
 * <p>Étant un record, elle se déstructure naturellement par record pattern
 * (JEP 440), par exemple dans un switch :</p>
 * <pre>{@code
 * switch (command) {
 *     case TransactionCommand(Long accountId, TransactionType type, BigDecimal amount) -> ...
 * }
 * }</pre>
 */
public record TransactionCommand(Long accountId, TransactionType type, BigDecimal amount) {

    /** Constructeur compact : valide les invariants dès la construction du record. */
    public TransactionCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("L'identifiant du compte est obligatoire");
        }
        if (type == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Le montant est obligatoire");
        }
    }
}
