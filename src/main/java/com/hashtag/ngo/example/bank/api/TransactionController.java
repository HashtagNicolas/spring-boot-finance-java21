package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.bean.TransactionService;
import com.hashtag.ngo.example.bank.entity.Transaction;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de gestion des transactions.
 */
@RestController
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/accounts/{id}/transactions")
    public TransactionResponse createTransaction(@PathVariable Long id, @RequestBody TransactionRequest request) {
        // Le compte pris en compte est celui de l'URL, quel que soit
        // l'accountId éventuellement porté par le corps de la requête.
        TransactionCommand command = transactionMapper.toCommand(id, request);
        Transaction transaction = transactionService.registerTransaction(command);
        return transactionMapper.toResponse(transaction);
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<TransactionResponse> getHistory(@PathVariable Long id) {
        return transactionService.getHistory(id).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    /**
     * Traite un lot de transactions en concurrence (threads virtuels, voir
     * {@link TransactionService#processBatch(List)}). Ici, contrairement
     * aux autres endpoints, {@code accountId} est bien lu dans le corps de
     * chaque élément, puisqu'aucune URL ne désigne un compte particulier.
     */
    @PostMapping("/transactions/batch")
    public List<TransactionResponse> processBatch(@RequestBody List<TransactionRequest> requests) {
        List<TransactionCommand> commands = requests.stream()
                .map(request -> transactionMapper.toCommand(request.accountId(), request))
                .toList();
        return transactionService.processBatch(commands).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
