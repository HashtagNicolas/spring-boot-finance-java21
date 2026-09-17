package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre exécuté une fois par requête : lit l'en-tête {@code Authorization},
 * valide le jeton JWT présent (le cas échéant) via {@link JwtService}, et
 * peuple le {@link SecurityContextHolder} pour que la suite de la chaîne
 * (et les contrôleurs) voient la requête comme authentifiée.
 *
 * <p>Si le jeton est absent ou invalide, ce filtre ne fait rien de plus et
 * laisse la requête continuer anonyme : c'est {@link SecurityConfig}, via
 * son {@code authenticationEntryPoint}, qui renverra alors 401 si
 * l'endpoint appelé exige une authentification.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtService.validateToken(token)) {
                String subject = jwtService.extractSubject(token);
                // Pas de rôles/autorités spécifiques : seule l'authentification
                // (isAuthenticated()) est requise par SecurityConfig, aucune
                // règle par rôle n'est appliquée pour ce squelette.
                var authentication = new UsernamePasswordAuthenticationToken(subject, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
