package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps liés à la création et à la consultation des comptes.
 */
public class AccountSteps {

    private final TestContext context;

    public AccountSteps(TestContext context) {
        this.context = context;
    }

    @Given("un compte courant pour {string} avec un solde initial de {string} et un découvert autorisé de {string}")
    public void unCompteCourantPour(String proprietaire, String soldeInitial, String decouvert) {
        creerCompteCourant(proprietaire, soldeInitial, decouvert);
    }

    @When("je crée un compte courant pour {string} avec un solde initial de {string} et un découvert autorisé de {string}")
    public void jeCreeUnCompteCourantPour(String proprietaire, String soldeInitial, String decouvert) {
        creerCompteCourant(proprietaire, soldeInitial, decouvert);
    }

    @Given("un compte épargne pour {string} avec un solde initial de {string} et un taux d'intérêt de {string}")
    public void unCompteEpargnePour(String proprietaire, String soldeInitial, String taux) {
        creerCompteEpargne(proprietaire, soldeInitial, taux);
    }

    @When("je crée un compte épargne pour {string} avec un solde initial de {string} et un taux d'intérêt de {string}")
    public void jeCreeUnCompteEpargnePour(String proprietaire, String soldeInitial, String taux) {
        creerCompteEpargne(proprietaire, soldeInitial, taux);
    }

    // Montants/taux en {string} plutôt qu'en {double} : {double} est analysé
    // selon la locale par défaut de la JVM (virgule en français), ce qui
    // rendrait ces steps fragiles selon l'environnement d'exécution. Le
    // constructeur BigDecimal(String), lui, est toujours indépendant de la
    // locale (point décimal uniquement).
    private void creerCompteCourant(String proprietaire, String soldeInitial, String decouvert) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("owner", proprietaire);
        body.put("type", "COURANT");
        body.put("initialBalance", new BigDecimal(soldeInitial));
        body.put("overdraftLimit", new BigDecimal(decouvert));
        creerCompte(proprietaire, body);
    }

    private void creerCompteEpargne(String proprietaire, String soldeInitial, String taux) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("owner", proprietaire);
        body.put("type", "EPARGNE");
        body.put("initialBalance", new BigDecimal(soldeInitial));
        body.put("interestRate", new BigDecimal(taux));
        creerCompte(proprietaire, body);
    }

    private void creerCompte(String proprietaire, Map<String, Object> body) {
        var response = context.post("/accounts", context.toJson(body), true);
        if (response.getStatusCode().value() == 201) {
            context.rememberAccount(proprietaire, context.lastResponseAsJson().get("id").asLong());
        }
    }

    @When("je dépose {string} sur ce compte")
    public void jeDeposeSurCeCompte(String montant) {
        Map<String, Object> body = Map.of("amount", new BigDecimal(montant));
        context.post("/accounts/" + context.currentAccountId() + "/deposit", context.toJson(body), true);
    }

    @When("je retire {string} de ce compte")
    public void jeRetireDeCeCompte(String montant) {
        Map<String, Object> body = Map.of("amount", new BigDecimal(montant));
        context.post("/accounts/" + context.currentAccountId() + "/withdraw", context.toJson(body), true);
    }

    @When("je capitalise les intérêts de ce compte")
    public void jeCapitaliseLesInteretsDeCeCompte() {
        context.post("/accounts/" + context.currentAccountId() + "/interest", true);
    }

    @Then("le compte a pour solde {string}")
    public void leCompteAPourSolde(String soldeAttendu) {
        assertThat(context.lastResponseAsJson().get("balance").decimalValue())
                .isEqualByComparingTo(new BigDecimal(soldeAttendu));
    }

    @Then("le compte de {string} a pour solde {string}")
    public void leCompteDeAPourSolde(String proprietaire, String soldeAttendu) {
        context.get("/accounts/" + context.accountId(proprietaire), true);
        assertThat(context.lastResponseAsJson().get("balance").decimalValue())
                .isEqualByComparingTo(new BigDecimal(soldeAttendu));
    }
}
