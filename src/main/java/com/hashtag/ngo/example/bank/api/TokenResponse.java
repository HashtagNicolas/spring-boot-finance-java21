package com.hashtag.ngo.example.bank.api;

/**
 * Jeton renvoyé après authentification.
 *
 * <p>Non utilisé pour l'instant (aucun {@code AuthController} n'existe
 * encore), mais introduit dès maintenant pour préparer la couche
 * sécurité.</p>
 */
public record TokenResponse(String token) {
}
