package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentification : échange des identifiants contre un jeton JWT.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Émission de jetons JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    @Operation(summary = "Authentifie un utilisateur et renvoie un jeton JWT")
    public TokenResponse createToken(@RequestBody LoginRequest request) {
        // Délègue la vérification des identifiants à Spring Security (via
        // UserDetailsConfig) ; lève une AuthenticationException si les
        // identifiants sont incorrects (traitée comme une erreur technique
        // générique par GlobalExceptionHandler pour ce squelette).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtService.generateToken(request.username());
        return new TokenResponse(token);
    }
}
