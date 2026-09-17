package com.hashtag.ngo.example.bank;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Point d'entrée exécuté par Maven Surefire (son nom se termine par "Test",
 * la convention de nommage par défaut de Surefire).
 *
 * <p>{@code @Suite} + {@code @IncludeEngines("cucumber")} délèguent la
 * découverte et l'exécution des tests au moteur JUnit Platform de Cucumber
 * (cucumber-junit-platform-engine), qui lit les fichiers {@code .feature}
 * indiqués par {@code @SelectClasspathResource} et exécute leurs scénarios
 * en s'appuyant sur les steps du package {@code cucumber} (voir
 * {@code GLUE_PROPERTY_NAME}).</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.hashtag.ngo.example.bank.cucumber")
public class RunCucumberTest {
}
