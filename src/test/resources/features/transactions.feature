# language: fr
Fonctionnalité: Transactions

  Contexte:
    Étant donné que je suis authentifié
    Et un compte courant pour "Chloé" avec un solde initial de "1000.00" et un découvert autorisé de "200.00"

  Scénario: Enregistrement de transactions et historique ordonné
    Quand j'enregistre un dépôt de "100.00" sur ce compte
    Et j'enregistre un retrait de "40.00" sur ce compte
    Alors l'historique de ce compte contient 2 transactions
    Et la dernière transaction de l'historique est un retrait de "40.00"

  Scénario: Traitement d'un lot de transactions en concurrence
    Étant donné un compte courant pour "David" avec un solde initial de "0.00" et un découvert autorisé de "0.00"
    Et un compte épargne pour "Emma" avec un solde initial de "0.00" et un taux d'intérêt de "0.01"
    Quand je soumets le lot de transactions suivant :
      | compte | type  | montant |
      | Chloé  | DEPOT | 10.00   |
      | David  | DEPOT | 20.00   |
      | Emma   | DEPOT | 30.00   |
    Alors la réponse a le statut 200
    Et le lot traité contient 3 transactions
    Et le compte de "Chloé" a pour solde "1010.00"
    Et le compte de "David" a pour solde "20.00"
    Et le compte de "Emma" a pour solde "30.00"
