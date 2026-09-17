package com.hashtag.ngo.example.bank.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

/**
 * Métadonnées globales de la documentation OpenAPI (générée par springdoc,
 * consultable via {@code /swagger-ui/index.html}) et déclaration du schéma
 * de sécurité "bearerAuth", référencé par {@code @SecurityRequirement} sur
 * les contrôleurs protégés.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de comptes bancaires",
                version = "0.1.0",
                description = "Gestion de comptes bancaires (courants/épargne) et de leurs transactions."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
