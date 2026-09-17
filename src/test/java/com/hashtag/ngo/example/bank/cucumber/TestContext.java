package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.spring.ScenarioScope;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * État partagé entre les classes de steps d'un même scénario.
 *
 * <p>{@code @ScenarioScope} (fourni par cucumber-spring) crée une nouvelle
 * instance de ce bean pour chaque scénario : aucun état ne fuite d'un
 * scénario à l'autre, même si le contexte Spring, lui, est démarré une
 * seule fois et partagé par toute l'exécution.</p>
 */
@Component
@ScenarioScope
public class TestContext {

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Long> accountIdsByOwner = new HashMap<>();

    private RestClient restClient;
    private String token;
    private Long currentAccountId;
    private ResponseEntity<String> lastResponse;

    private RestClient client() {
        if (restClient == null) {
            // Construit avec le port aléatoire attribué par
            // @SpringBootTest(webEnvironment = RANDOM_PORT) : lu paresseusement
            // (et non au constructeur) pour être sûr que Spring a déjà injecté
            // le champ @Value avant la première utilisation.
            restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        }
        return restClient;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void rememberAccount(String owner, long accountId) {
        accountIdsByOwner.put(owner, accountId);
        this.currentAccountId = accountId;
    }

    public long accountId(String owner) {
        Long id = accountIdsByOwner.get(owner);
        if (id == null) {
            throw new IllegalStateException("Aucun compte connu pour : " + owner);
        }
        return id;
    }

    public long currentAccountId() {
        if (currentAccountId == null) {
            throw new IllegalStateException("Aucun compte courant n'a été établi dans ce scénario");
        }
        return currentAccountId;
    }

    public ResponseEntity<String> lastResponse() {
        return lastResponse;
    }

    public JsonNode lastResponseAsJson() {
        try {
            return objectMapper.readTree(lastResponse.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Réponse JSON invalide : " + lastResponse.getBody(), e);
        }
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de sérialiser en JSON : " + value, e);
        }
    }

    public ResponseEntity<String> post(String path, boolean withToken) {
        return execute(() -> client().post()
                .uri(path)
                .headers(headers -> applyAuth(headers, withToken, null))
                .retrieve()
                .toEntity(String.class));
    }

    public ResponseEntity<String> post(String path, String jsonBody, boolean withToken) {
        return execute(() -> client().post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> applyAuth(headers, withToken, null))
                .body(jsonBody)
                .retrieve()
                .toEntity(String.class));
    }

    public ResponseEntity<String> get(String path, boolean withToken) {
        return execute(() -> client().get()
                .uri(path)
                .headers(headers -> applyAuth(headers, withToken, null))
                .retrieve()
                .toEntity(String.class));
    }

    /** Variante utilisée pour tester explicitement un jeton (valide ou non) différent de celui du scénario. */
    public ResponseEntity<String> get(String path, String explicitToken) {
        return execute(() -> client().get()
                .uri(path)
                .headers(headers -> applyAuth(headers, true, explicitToken))
                .retrieve()
                .toEntity(String.class));
    }

    private void applyAuth(HttpHeaders headers, boolean withToken, String explicitToken) {
        if (explicitToken != null) {
            headers.setBearerAuth(explicitToken);
        } else if (withToken && token != null) {
            headers.setBearerAuth(token);
        }
    }

    /**
     * RestClient (contrairement à l'ancien TestRestTemplate, supprimé en
     * Spring Boot 4) lève une exception pour tout statut 4xx/5xx au lieu de
     * renvoyer normalement une réponse. On la convertit ici en
     * {@code ResponseEntity} classique, pour que les steps "Alors" puissent
     * asserter sur n'importe quel statut HTTP sans avoir à gérer de
     * try/catch dans chaque step.
     */
    private ResponseEntity<String> execute(Supplier<ResponseEntity<String>> call) {
        try {
            lastResponse = call.get();
        } catch (RestClientResponseException e) {
            lastResponse = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
        return lastResponse;
    }
}
