package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logique métier relative aux comptes bancaires : création, consultation et
 * application des opérations de dépôt/retrait.
 */
public interface AccountService {

    /** Crée et persiste un nouveau compte courant. */
    Account createCheckingAccount(String owner, BigDecimal initialBalance, BigDecimal overdraftLimit);

    /** Crée et persiste un nouveau compte épargne. */
    Account createSavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate);

    /** Consulte un compte par son identifiant. */
    Account getAccount(Long accountId);

    /** Liste l'ensemble des comptes. */
    List<Account> listAccounts();

    /**
     * Applique un dépôt ou un retrait (selon {@link TransactionCommand#type()})
     * au compte désigné par {@link TransactionCommand#accountId()}, puis
     * persiste le nouveau solde.
     */
    Account applyTransaction(TransactionCommand command);

    /**
     * Montant maximal qu'il est possible de retirer immédiatement du compte :
     * dépend du type de compte (le découvert n'existe que sur un compte courant).
     */
    BigDecimal getAvailableBalance(Long accountId);
}
