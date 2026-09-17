package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Utilisateur de démonstration et infrastructure d'authentification
 * (encodeur de mot de passe, gestionnaire d'authentification) utilisés par
 * {@code POST /auth/token}.
 *
 * <p>Un unique utilisateur en mémoire ({@code demo}/{@code demo123}) : à
 * remplacer par une vraie source d'utilisateurs (base de données...)
 * lorsque le besoin se présentera.</p>
 */
@Configuration
public class UserDetailsConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails demoUser = User.withUsername("demo")
                .password(passwordEncoder.encode("demo123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(demoUser);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
