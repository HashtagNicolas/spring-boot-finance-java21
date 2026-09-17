package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte épargne : rémunéré à un taux d'intérêt {@link #interestRate}, sans
 * découvert autorisé.
 *
 * <p>Classe {@code final} pour la même raison que {@link CheckingAccount}
 * (voir sa Javadoc) : {@link Account} est {@code sealed}.</p>
 */
@Entity
@DiscriminatorValue("EPARGNE")
public final class SavingsAccount extends Account {

    @Column(name = "interest_rate", precision = 6, scale = 4)
    private BigDecimal interestRate;

    protected SavingsAccount() {
        super();
    }

    public SavingsAccount(String owner, BigDecimal balance, BigDecimal interestRate) {
        super(owner, balance);
        this.interestRate = interestRate;
    }

    /**
     * Retire un montant. Un compte épargne n'autorise aucun découvert : le
     * solde doit rester supérieur ou égal à zéro après l'opération.
     */
    @Override
    public void withdraw(BigDecimal amount) {
        requirePositiveAmount(amount);
        if (getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Solde insuffisant : le découvert n'est pas autorisé sur un compte épargne");
        }
        debit(amount);
    }

    /** Capitalise les intérêts en créditant le compte du solde courant multiplié par le taux. */
    public void applyInterest() {
        BigDecimal interest = getBalance().multiply(interestRate);
        credit(interest);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }
}
