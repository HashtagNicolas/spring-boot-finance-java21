package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import org.hibernate.annotations.ConcreteProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compte bancaire générique.
 *
 * <p>Classe scellée : seuls {@link CheckingAccount} (compte courant) et
 * {@link SavingsAccount} (compte épargne) peuvent en hériter, ce qui reflète
 * fidèlement le nombre fini de types de comptes gérés par l'application.</p>
 *
 * <p>Stratégie d'héritage JPA {@code SINGLE_TABLE} : toutes les propriétés
 * (communes et spécifiques) sont stockées dans une seule table {@code accounts},
 * avec une colonne discriminante {@code account_type} qui indique le sous-type
 * réel de chaque ligne. C'est la stratégie la plus performante en lecture
 * (aucune jointure), au prix de colonnes nullable spécifiques à chaque sous-type.</p>
 *
 * <p>Note sur les proxies Hibernate : par défaut, Hibernate génère à
 * l'exécution une sous-classe proxy de chaque entité pour permettre un
 * chargement différé (LAZY). Une classe {@code sealed} ne pouvant être
 * étendue que par sa clause {@code permits}, la génération de
 * {@code Account$HibernateProxy} échouerait. L'annotation
 * {@code @ConcreteProxy} (Hibernate 7) résout ceci : Hibernate génère alors
 * un proxy par sous-classe concrète ({@code CheckingAccount},
 * {@code SavingsAccount}) au lieu d'un proxy de la classe abstraite
 * scellée.</p>
 */
@ConcreteProxy
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
public abstract sealed class Account permits CheckingAccount, SavingsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Constructeur sans argument requis par la spécification JPA. */
    protected Account() {
    }

    protected Account(String owner, BigDecimal balance) {
        this.owner = owner;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Dépose un montant sur le compte.
     *
     * <p>Contrairement au retrait, un dépôt suit exactement la même règle
     * quel que soit le type de compte : il est donc implémenté une seule
     * fois ici plutôt que délégué aux sous-classes.</p>
     */
    public void deposit(BigDecimal amount) {
        requirePositiveAmount(amount);
        credit(amount);
    }

    /**
     * Retire un montant du compte.
     *
     * <p>Chaque type de compte applique sa propre règle d'autorisation
     * (plafond de découvert pour un compte courant, interdiction de solde
     * négatif pour un compte épargne) : cette opération est donc abstraite
     * et implémentée par chaque sous-classe.</p>
     */
    public abstract void withdraw(BigDecimal amount);

    /** Vérifie qu'un montant d'opération est valide (non nul et strictement positif). */
    protected static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Le montant de l'opération doit être strictement positif");
        }
    }

    /** Augmente le solde ; réservé aux sous-classes pour implémenter leurs propres règles. */
    protected void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /** Diminue le solde ; réservé aux sous-classes pour implémenter leurs propres règles. */
    protected void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
