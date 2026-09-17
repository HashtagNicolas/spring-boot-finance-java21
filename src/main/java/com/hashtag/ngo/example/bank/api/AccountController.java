package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.TransactionType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de gestion des comptes. Protégés par JWT (voir
 * {@code SecurityConfig}) ; les exceptions métier sont traduites en
 * réponses HTTP par {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/accounts")
@Tag(name = "Comptes", description = "Création, consultation, dépôts et retraits")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @Operation(summary = "Crée un compte courant ou épargne")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest request) {
        // Pattern matching for switch sur AccountType : choisit le sous-type
        // concret à créer (CheckingAccount ou SavingsAccount).
        Account account = switch (request.type()) {
            case COURANT -> accountService.createCheckingAccount(
                    request.owner(), request.initialBalance(), request.overdraftLimit());
            case EPARGNE -> accountService.createSavingsAccount(
                    request.owner(), request.initialBalance(), request.interestRate());
        };
        AccountResponse response = accountMapper.toResponse(account);
        // 201 Created + en-tête Location, plutôt que 200 : la requête a créé
        // une nouvelle ressource, dont l'URL de consultation est connue.
        return ResponseEntity.created(URI.create("/accounts/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulte un compte par son identifiant")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.getAccount(id));
    }

    @GetMapping
    @Operation(summary = "Liste l'ensemble des comptes")
    public List<AccountResponse> listAccounts() {
        return accountService.listAccounts().stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Dépose un montant sur le compte")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        // Le type est imposé par l'URL : celui éventuellement présent dans
        // le corps de la requête (le cas échéant) est ignoré.
        TransactionCommand command = new TransactionCommand(id, TransactionType.DEPOT, request.amount());
        return accountMapper.toResponse(accountService.applyTransaction(command));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Retire un montant du compte")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        TransactionCommand command = new TransactionCommand(id, TransactionType.RETRAIT, request.amount());
        return accountMapper.toResponse(accountService.applyTransaction(command));
    }

    @PostMapping("/{id}/interest")
    @Operation(summary = "Capitalise les intérêts d'un compte épargne (sans effet sur un compte courant)")
    public AccountResponse applyInterest(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.applyInterest(id));
    }
}
