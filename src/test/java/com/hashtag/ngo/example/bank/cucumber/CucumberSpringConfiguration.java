package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Point de branchement entre Cucumber et Spring Boot.
 *
 * <p>Cette classe ne contient aucun step : {@code @CucumberContextConfiguration}
 * indique à cucumber-spring d'utiliser le contexte Spring configuré ici
 * (démarré une seule fois, puis réutilisé pour tous les scénarios) pour
 * instancier les classes de steps par injection de dépendances.</p>
 *
 * <p>{@code webEnvironment = RANDOM_PORT} démarre un vrai serveur Tomcat sur
 * un port libre : les steps appellent l'API par de vraies requêtes HTTP
 * (voir {@link TestContext}), plutôt que d'invoquer les contrôleurs
 * directement.</p>
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CucumberSpringConfiguration {
}
