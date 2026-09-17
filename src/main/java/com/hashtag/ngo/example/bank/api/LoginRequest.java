package com.hashtag.ngo.example.bank.api;

/**
 * Corps de la requête d'authentification ({@code POST /auth/token}).
 */
public record LoginRequest(String username, String password) {
}
