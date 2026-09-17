package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Transaction;

import java.util.List;
import java.util.SequencedCollection;

/**
 * Logique métier relative aux transactions : enregistrement, traitement par
 * lots et consultation de l'historique d'un compte.
 */
public interface TransactionService {

    /** Applique la commande au compte concerné, puis enregistre la transaction correspondante. */
    Transaction registerTransaction(TransactionCommand command);

    /**
     * Traite un lot de commandes en concurrence (un thread virtuel par
     * commande) et renvoie les transactions enregistrées, dans le même
     * ordre que les commandes fournies.
     */
    List<Transaction> processBatch(List<TransactionCommand> commands);

    /** Historique des transactions d'un compte, du plus ancien au plus récent. */
    SequencedCollection<Transaction> getHistory(Long accountId);

    /** Historique des transactions d'un compte, du plus récent au plus ancien. */
    SequencedCollection<Transaction> getHistoryMostRecentFirst(Long accountId);

    /** Dernière transaction enregistrée pour un compte. */
    Transaction getLastTransaction(Long accountId);
}
