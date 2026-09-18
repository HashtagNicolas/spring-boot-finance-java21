package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Implémentation basée sur la bibliothèque JJWT (io.jsonwebtoken).
 *
 * <p>La clé de signature est dérivée du secret {@code jwt.secret} (voir
 * application.yml), plutôt que générée aléatoirement : ainsi, les jetons
 * émis restent valides après un redémarrage de l'application, tant que le
 * secret ne change pas.</p>
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final SecretKey signingKey;

    public JwtServiceImpl(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

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
