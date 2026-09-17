package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.databind.JsonNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps liés à l'enregistrement des transactions, à leur historique et à
 * leur traitement par lot.
 */
public class TransactionSteps {

    private final TestContext context;

    public TransactionSteps(TestContext context) {
        this.context = context;
    }

    @When("j'enregistre un dépôt de {string} sur ce compte")
    public void jEnregistreUnDepotSurCeCompte(String montant) {
        enregistrer("DEPOT", montant);
    }

    @When("j'enregistre un retrait de {string} sur ce compte")
    public void jEnregistreUnRetraitSurCeCompte(String montant) {
        enregistrer("RETRAIT", montant);
    }

    private void enregistrer(String type, String montant) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", type);
        body.put("amount", new BigDecimal(montant));
        context.post("/accounts/" + context.currentAccountId() + "/transactions", context.toJson(body), true);
    }

    @Then("l'historique de ce compte contient {int} transactions")
    public void lHistoriqueDeCeCompteContientTransactions(int nombreAttendu) {
        assertThat(historiqueDuCompte(context.currentAccountId())).hasSize(nombreAttendu);
    }

    @Then("la dernière transaction de l'historique est un retrait de {string}")
    public void laDerniereTransactionEstUnRetraitDe(String montantAttendu) {
        List<JsonNode> historique = historiqueDuCompte(context.currentAccountId());
        // getLast() (Sequenced Collections, JEP 431) côté service : l'API
        // renvoie l'historique du plus ancien au plus récent, donc le
        // dernier élément du tableau JSON correspond à ce que renverrait
        // TransactionService.getLastTransaction().
        JsonNode derniere = historique.get(historique.size() - 1);
        assertThat(derniere.get("type").asText()).isEqualTo("RETRAIT");
        assertThat(derniere.get("amount").decimalValue()).isEqualByComparingTo(new BigDecimal(montantAttendu));
    }

    private List<JsonNode> historiqueDuCompte(long accountId) {
        var response = context.get("/accounts/" + accountId + "/transactions", true);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<JsonNode> items = new ArrayList<>();
        context.lastResponseAsJson().forEach(items::add);
        return items;
    }

    @When("je soumets le lot de transactions suivant :")
    public void jeSoumetsLeLotDeTransactionsSuivant(DataTable table) {
        List<Map<String, Object>> commandes = new ArrayList<>();
        for (Map<String, String> ligne : table.asMaps(String.class, String.class)) {
            Map<String, Object> commande = new LinkedHashMap<>();
            commande.put("accountId", context.accountId(ligne.get("compte")));
            commande.put("type", ligne.get("type"));
            commande.put("amount", new BigDecimal(ligne.get("montant")));
            commandes.add(commande);
        }
        context.post("/transactions/batch", context.toJson(commandes), true);
    }

    @Then("le lot traité contient {int} transactions")
    public void leLotTraiteContientTransactions(int nombreAttendu) {
        assertThat(context.lastResponseAsJson()).hasSize(nombreAttendu);
    }
}
