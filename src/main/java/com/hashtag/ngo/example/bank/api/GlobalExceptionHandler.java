package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountNotFoundException;
import com.hashtag.ngo.example.bank.entity.InsufficientFundsException;
import com.hashtag.ngo.example.bank.entity.InvalidAmountException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Traduit les exceptions en réponses HTTP homogènes ({@link ErrorResponse}),
 * plutôt que de laisser Spring MVC renvoyer ses pages d'erreur par défaut.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException exception) {
        return toResponse(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException exception) {
        return toResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", exception.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException exception) {
        return toResponse(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", exception.getMessage());
    }

    /**
     * Filet de sécurité pour toute exception non prévue ci-dessus : on ne
     * renvoie jamais son message ni sa trace de pile au client (fuite
     * d'information interne), seulement un message générique. Le détail est
     * journalisé côté serveur pour le diagnostic.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Erreur technique inattendue", exception);
        return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Une erreur technique est survenue");
    }

    private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, String code, String message) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), code, message);
        return ResponseEntity.status(status).body(body);
    }
}
