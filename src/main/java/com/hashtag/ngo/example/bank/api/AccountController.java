package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.TransactionType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de gestion des comptes.
 *
 * <p>Aucune gestion d'erreur globale pour l'instant : les exceptions métier
 * ({@code AccountNotFoundException}, {@code InsufficientFundsException},
 * {@code InvalidAmountException}) remontent telles quelles (500 par
 * défaut) ; un {@code @ControllerAdvice} les traduira en réponses HTTP
 * adaptées dans une prochaine itération.</p>
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    public AccountResponse createAccount(@RequestBody AccountRequest request) {
        // Pattern matching for switch sur AccountType : choisit le sous-type
        // concret à créer (CheckingAccount ou SavingsAccount).
        Account account = switch (request.type()) {
            case COURANT -> accountService.createCheckingAccount(
                    request.owner(), request.initialBalance(), request.overdraftLimit());
            case EPARGNE -> accountService.createSavingsAccount(
                    request.owner(), request.initialBalance(), request.interestRate());
        };
        return accountMapper.toResponse(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.getAccount(id));
    }

    @GetMapping
    public List<AccountResponse> listAccounts() {
        return accountService.listAccounts().stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        // Le type est imposé par l'URL : celui éventuellement présent dans
        // le corps de la requête (le cas échéant) est ignoré.
        TransactionCommand command = new TransactionCommand(id, TransactionType.DEPOT, request.amount());
        return accountMapper.toResponse(accountService.applyTransaction(command));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        TransactionCommand command = new TransactionCommand(id, TransactionType.RETRAIT, request.amount());
        return accountMapper.toResponse(accountService.applyTransaction(command));
    }
}
