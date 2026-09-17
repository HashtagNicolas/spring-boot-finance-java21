# language: fr
Fonctionnalité: Authentification

  Scénario: Accès refusé sans jeton
    Quand j'appelle GET /accounts sans jeton
    Alors la réponse a le statut 401

  Scénario: Accès refusé avec un jeton invalide
    Quand j'appelle GET /accounts avec le jeton invalide "abc.def.ghi"
    Alors la réponse a le statut 401

  Scénario: Accès autorisé avec un jeton valide
    Étant donné que je suis authentifié
    Quand j'appelle GET /accounts avec mon jeton
    Alors la réponse a le statut 200
