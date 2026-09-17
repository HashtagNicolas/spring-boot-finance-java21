package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mouvement (dépôt ou retrait) appliqué à un {@link Account}.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /*
     * Attention : cette association est volontairement chargée en EAGER.
     * Par défaut, Hibernate charge les associations @ManyToOne en différé
     * (LAZY) en générant, au runtime, un proxy qui sous-classe le type de
     * l'entité cible. Or Account n'est étendue que par CheckingAccount et
     * SavingsAccount, qui sont toutes deux `final` : aucun proxy ne peut
     * être généré pour elles, ce qui ferait échouer le chargement différé.
     * Le fetch EAGER explicite évite ce piège.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Constructeur sans argument requis par la spécification JPA. */
    protected Transaction() {
    }

    public Transaction(TransactionType type, BigDecimal amount, Account account) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Account getAccount() {
        return account;
    }
}
