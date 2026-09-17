package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps liés à l'authentification et au contrôle d'accès JWT.
 */
public class AuthSteps {

    private final TestContext context;

    public AuthSteps(TestContext context) {
        this.context = context;
    }

    // Le mot-clé Gherkin français "Étant donné que" est un seul token pour
    // Cucumber (le "que" en fait partie) : le texte du step ne le répète pas.
    @Given("je suis authentifié")
    public void jeSuisAuthentifie() {
        String body = context.toJson(Map.of("username", "demo", "password", "demo123"));
        var response = context.post("/auth/token", body, false);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        context.setToken(context.lastResponseAsJson().get("token").asText());
    }

    // Les "/" sont échappés en "\/" : dans une expression Cucumber, "/" sert
    // à délimiter des alternatives (ex. "a/b" veut dire "a" ou "b"), donc un
    // "/" littéral doit être échappé pour être pris tel quel.
    @When("j'appelle GET \\/accounts sans jeton")
    public void jAppelleAccountsSansJeton() {
        context.get("/accounts", false);
    }

    @When("j'appelle GET \\/accounts avec le jeton invalide {string}")
    public void jAppelleAccountsAvecJetonInvalide(String jetonInvalide) {
        context.get("/accounts", jetonInvalide);
    }

    @When("j'appelle GET \\/accounts avec mon jeton")
    public void jAppelleAccountsAvecMonJeton() {
        context.get("/accounts", true);
    }

    @Then("la réponse a le statut {int}")
    public void laReponseALeStatut(int statutAttendu) {
        assertThat(context.lastResponse().getStatusCode().value()).isEqualTo(statutAttendu);
    }
}
