package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité HTTP : API sans état (aucune session, pas de
 * cookie), protégée par JWT plutôt que par CSRF/session.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless authentifiée par jeton : la protection CSRF
                // (pensée pour les sessions basées sur des cookies) n'a pas lieu d'être.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/token", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/accounts/**", "/transactions/**").authenticated()
                        .anyRequest().authenticated())
                // Sans ceci, Spring Security renvoie par défaut 403 (Forbidden)
                // pour une requête non authentifiée sur un endpoint protégé ;
                // on veut ici 401 (Unauthorized), qu'un jeton soit absent ou invalide.
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
