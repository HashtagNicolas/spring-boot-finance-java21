package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;

/**
 * Corps de la requête d'une transaction.
 *
 * <p>Pour les endpoints dédiés {@code /accounts/{id}/deposit} et
 * {@code /accounts/{id}/withdraw}, {@code accountId} et {@code type} sont
 * ignorés : le compte vient de l'URL et le type (dépôt/retrait) est imposé
 * par l'endpoint appelé. Ces deux champs ne sont réellement nécessaires que
 * pour {@code POST /transactions/batch}, où chaque élément du lot désigne
 * son propre compte.</p>
 */
public record TransactionRequest(Long accountId, TransactionType type, BigDecimal amount) {
}
