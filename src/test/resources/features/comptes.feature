# language: fr
Fonctionnalité: Gestion des comptes bancaires

  Contexte:
    Étant donné que je suis authentifié

  Scénario: Création d'un compte courant
    Quand je crée un compte courant pour "Alice" avec un solde initial de "1000.00" et un découvert autorisé de "200.00"
    Alors la réponse a le statut 201
    Et le compte a pour solde "1000.00"

  Scénario: Création d'un compte épargne
    Quand je crée un compte épargne pour "Bob" avec un solde initial de "500.00" et un taux d'intérêt de "0.05"
    Alors la réponse a le statut 201
    Et le compte a pour solde "500.00"

  Scénario: Dépôt sur un compte courant
    Étant donné un compte courant pour "Alice" avec un solde initial de "1000.00" et un découvert autorisé de "200.00"
    Quand je dépose "150.00" sur ce compte
    Alors la réponse a le statut 200
    Et le compte a pour solde "1150.00"

  Scénario: Retrait autorisé sur un compte courant
    Étant donné un compte courant pour "Alice" avec un solde initial de "1000.00" et un découvert autorisé de "200.00"
    Quand je retire "150.00" de ce compte
    Alors la réponse a le statut 200
    Et le compte a pour solde "850.00"

  Scénario: Retrait refusé pour solde insuffisant
    Étant donné un compte épargne pour "Bob" avec un solde initial de "500.00" et un taux d'intérêt de "0.05"
    Quand je retire "600.00" de ce compte
    Alors la réponse a le statut 400

  Scénario: Capitalisation des intérêts sur un compte épargne
    Étant donné un compte épargne pour "Bob" avec un solde initial de "1000.00" et un taux d'intérêt de "0.05"
    Quand je capitalise les intérêts de ce compte
    Alors la réponse a le statut 200
    Et le compte a pour solde "1050.00"
