package com.hashtag.ngo.example.bank.bean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Implémentation basée sur la bibliothèque JJWT (io.jsonwebtoken).
 *
 * <p>La clé de signature est générée une seule fois, à la construction de ce
 * bean singleton. Elle n'est pas persistée : les jetons émis avant un
 * redémarrage de l'application ne sont donc plus valides après. C'est
 * acceptable pour ce squelette ; une vraie clé secrète externalisée
 * (configuration, gestionnaire de secrets...) serait nécessaire en
 * production.</p>
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final SecretKey signingKey = Jwts.SIG.HS256.key().build();

    @Override
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_VALIDITY)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
