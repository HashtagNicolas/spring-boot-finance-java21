package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte courant : autorise un découvert dans la limite de {@link #overdraftLimit}.
 *
 * <p>Classe {@code final} : elle ne peut pas être sous-classée, ce qui est
 * imposé par le fait qu'{@link Account} est {@code sealed} et ne permet que
 * deux sous-types directs. Cela a une conséquence importante sur les
 * associations {@code @ManyToOne} qui pointent vers {@link Account} (voir
 * {@link Transaction}) : Hibernate ne peut pas générer de proxy de chargement
 * différé (LAZY) pour un type final.</p>
 */
@Entity
@DiscriminatorValue("COURANT")
public final class CheckingAccount extends Account {

    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    protected CheckingAccount() {
        super();
    }

    public CheckingAccount(String owner, BigDecimal balance, BigDecimal overdraftLimit) {
        super(owner, balance);
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Retire un montant, en autorisant le solde à devenir négatif jusqu'à
     * concurrence de {@code -overdraftLimit}.
     */
    @Override
    public void withdraw(BigDecimal amount) {
        requirePositiveAmount(amount);
        BigDecimal balanceAfterWithdrawal = getBalance().subtract(amount);
        if (balanceAfterWithdrawal.compareTo(overdraftLimit.negate()) < 0) {
            throw new InsufficientFundsException("Le retrait dépasse le découvert autorisé");
        }
        debit(amount);
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}
