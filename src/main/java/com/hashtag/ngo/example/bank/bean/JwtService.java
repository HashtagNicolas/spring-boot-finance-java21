package com.hashtag.ngo.example.bank.bean;

/**
 * Émission et validation de jetons JWT.
 *
 * <p>Ce service n'a aucune dépendance à Spring MVC : il n'utilise que la
 * bibliothèque JJWT et reste donc utilisable en dehors d'un contexte web
 * (traitement batch, tests, ligne de commande...).</p>
 */
public interface JwtService {

    /** Génère un jeton signé pour le sujet donné (par exemple un identifiant utilisateur). */
    String generateToken(String subject);

    /** Indique si le jeton est syntaxiquement valide, correctement signé, et non expiré. */
    boolean validateToken(String token);

    /** Extrait le sujet (claim "subject") porté par le jeton. */
    String extractSubject(String token);
}
