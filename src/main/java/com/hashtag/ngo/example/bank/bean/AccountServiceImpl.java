package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.AccountRepository;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import com.hashtag.ngo.example.bank.entity.TransactionType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createCheckingAccount(String owner, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        return accountRepository.save(new CheckingAccount(owner, initialBalance, overdraftLimit));
    }

    @Override
    public Account createSavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate) {
        return accountRepository.save(new SavingsAccount(owner, initialBalance, interestRate));
    }

    @Override
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Compte introuvable : " + accountId));
    }

    @Override
    public List<Account> listAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account applyTransaction(TransactionCommand command) {
        /*
         * Record pattern (JEP 440) : les composants du record TransactionCommand
         * sont extraits directement dans le pattern du switch (accountId, type,
         * amount) plutôt que via les accesseurs accountId()/type()/amount().
         *
         * Le switch est exhaustif sans clause default : TransactionCommand
         * est un record, donc implicitement final, et cet unique pattern
         * couvre déjà tous les cas possibles du type statique de `command`.
         */
        return switch (command) {
            case TransactionCommand(Long accountId, TransactionType type, BigDecimal amount) -> {
                Account account = getAccount(accountId);
                if (type == TransactionType.DEPOT) {
                    account.deposit(amount);
                } else {
                    account.withdraw(amount);
                }
                yield accountRepository.save(account);
            }
        };
    }

    @Override
    public BigDecimal getAvailableBalance(Long accountId) {
        Account account = getAccount(accountId);
        /*
         * Pattern matching for switch (JEP 441) sur la hiérarchie scellée
         * Account : exhaustif sans clause default, le compilateur sachant
         * que CheckingAccount et SavingsAccount sont les deux seuls
         * sous-types permis (clause `permits` de Account).
         */
        return switch (account) {
            case CheckingAccount checking -> checking.getBalance().add(checking.getOverdraftLimit());
            case SavingsAccount savings -> savings.getBalance();
        };
    }
}
