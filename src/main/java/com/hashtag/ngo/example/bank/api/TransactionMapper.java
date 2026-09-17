package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.entity.Transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversion entre les entités {@link Transaction} et les représentations
 * API correspondantes, ainsi que la construction des commandes
 * {@link TransactionCommand} consommées par la couche bean.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountId", source = "account.id")
    TransactionResponse toResponse(Transaction transaction);

    /**
     * Construit la commande à partir de l'identifiant de compte (venant
     * généralement de l'URL) et du corps de la requête, dont seuls
     * {@code type} et {@code amount} sont repris.
     */
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "amount", source = "request.amount")
    TransactionCommand toCommand(Long accountId, TransactionRequest request);
}
