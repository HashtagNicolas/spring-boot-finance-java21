package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversion des entités {@link Account} vers leur représentation API
 * {@link AccountResponse}.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Point d'entrée utilisé par les contrôleurs : leur variable est de
     * type statique {@code Account} (la hiérarchie scellée), donc le choix
     * du sous-type se fait ici, à l'exécution, par pattern matching for
     * switch — une simple surcharge Java ne le permettrait pas, puisqu'elle
     * se résout à la compilation sur le type statique de l'argument.
     */
    default AccountResponse toResponse(Account account) {
        return switch (account) {
            case CheckingAccount checking -> toResponse(checking);
            case SavingsAccount savings -> toResponse(savings);
        };
    }

    @Mapping(target = "type", constant = "COURANT")
    @Mapping(target = "interestRate", ignore = true)
    AccountResponse toResponse(CheckingAccount account);

    @Mapping(target = "type", constant = "EPARGNE")
    @Mapping(target = "overdraftLimit", ignore = true)
    AccountResponse toResponse(SavingsAccount account);
}
