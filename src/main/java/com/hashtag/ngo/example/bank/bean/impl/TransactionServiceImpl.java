package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.bean.TransactionService;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.Transaction;
import com.hashtag.ngo.example.bank.entity.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction registerTransaction(TransactionCommand command) {
        Account account = accountService.applyTransaction(command);

        // Pattern matching for switch sur l'enum TransactionType : exhaustif
        // sans clause default, les deux seules constantes étant couvertes.
        String message = switch (command.type()) {
            case DEPOT -> "Dépôt de %s sur le compte %d".formatted(command.amount(), command.accountId());
            case RETRAIT -> "Retrait de %s depuis le compte %d".formatted(command.amount(), command.accountId());
        };
        log.info(message);

        Transaction transaction = new Transaction(command.type(), command.amount(), account);
        return transactionRepository.save(transaction);
    }

    /**
     * Traite le lot en concurrence à l'aide de threads virtuels
     * ({@link Executors#newVirtualThreadPerTaskExecutor()}).
     *
     * <p>Chaque commande donne lieu à une tâche courte et majoritairement
     * bloquante (lecture puis écriture JDBC). Ouvrir un thread de plateforme
     * (OS) par tâche serait coûteux en mémoire et en changements de contexte
     * dès que le lot grossit. Les threads virtuels sont conçus pour ce cas :
     * leur création est quasi gratuite (on peut en démarrer des centaines de
     * milliers) et, lorsqu'une tâche bloque sur une E/S, le thread porteur
     * (OS) sous-jacent est libéré pour exécuter un autre thread virtuel
     * plutôt que de rester inactif.</p>
     */
    @Override
    public List<Transaction> processBatch(List<TransactionCommand> commands) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Transaction>> futures = commands.stream()
                    .map(command -> executor.submit(() -> registerTransaction(command)))
                    .toList();

            return futures.stream()
                    .map(this::join)
                    .toList();
        }
    }

    private Transaction join(Future<Transaction> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Traitement du lot de transactions interrompu", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Échec du traitement d'une transaction du lot", e.getCause());
        }
    }

    @Override
    public SequencedCollection<Transaction> getHistory(Long accountId) {
        /*
         * Utilise uniquement le repository existant (pas de requête dédiée
         * ajoutée) : le filtrage et le tri sont effectués ici. Depuis Java 21,
         * List<T> implémente SequencedCollection, ce qui donne accès à
         * getFirst()/getLast()/reversed() sur la liste triée renvoyée.
         */
        return transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAccount().getId().equals(accountId))
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .toList();
    }

    @Override
    public SequencedCollection<Transaction> getHistoryMostRecentFirst(Long accountId) {
        // reversed() (Sequenced Collections, JEP 431) : vue inversée, sans
        // retrier ni copier explicitement la liste sous-jacente.
        return getHistory(accountId).reversed();
    }

    @Override
    public Transaction getLastTransaction(Long accountId) {
        SequencedCollection<Transaction> history = getHistory(accountId);
        if (history.isEmpty()) {
            throw new NoSuchElementException("Aucune transaction pour le compte : " + accountId);
        }
        // getLast() (Sequenced Collections, JEP 431)
        return history.getLast();
    }
}
