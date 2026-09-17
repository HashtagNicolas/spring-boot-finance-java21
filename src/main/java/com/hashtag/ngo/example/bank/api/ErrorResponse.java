package com.hashtag.ngo.example.bank.api;

import java.time.Instant;

/**
 * Corps de réponse en cas d'erreur.
 *
 * <p>Pas encore utilisé (aucun gestionnaire global d'exceptions n'existe
 * encore), mais introduit dès maintenant : il sera renvoyé par le futur
 * {@code @ControllerAdvice} qui traduira les exceptions métier
 * ({@code AccountNotFoundException}, {@code InsufficientFundsException},
 * {@code InvalidAmountException}...) en réponses HTTP.</p>
 *
 * @param timestamp instant auquel l'erreur s'est produite
 * @param status    code de statut HTTP (ex. 404, 400, 409)
 * @param code      code métier stable identifiant le type d'erreur (ex. "ACCOUNT_NOT_FOUND")
 * @param message   message lisible décrivant l'erreur
 */
public record ErrorResponse(Instant timestamp, int status, String code, String message) {
}
