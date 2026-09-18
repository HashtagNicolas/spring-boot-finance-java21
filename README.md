# API de comptes bancaires — Transactions

Petit projet pédagogique : une API REST de gestion de comptes bancaires
(comptes courants et comptes épargne) et de leurs transactions (dépôts,
retraits), servant surtout de prétexte pour illustrer, sur un cas concret,
les nouveautés apportées par **Java 21** et un empilement **Spring Boot 4** à
jour (sécurité JWT, documentation OpenAPI, tests BDD, tests d'architecture).

Le projet est volontairement petit : pas de vraie base de données (H2 en
mémoire), pas de gestion des rôles avancée, un seul utilisateur de
démonstration. L'objectif est de montrer *comment* s'articulent les
différentes briques, pas de livrer un produit complet.

## 1. Présentation

- **Domaine** : deux types de comptes (`CheckingAccount`/compte courant,
  `SavingsAccount`/compte épargne), qui partagent une notion commune de
  `Account`, et des `Transaction` (dépôt ou retrait) qui leur sont liées.
- **Stack** : Java 21, Spring Boot 4.1.x (Spring Framework 7, Spring
  Security, Spring Data JPA), Maven.
- **Persistance** : H2 en mémoire (les données sont réinitialisées à chaque
  démarrage — voir `spring.jpa.hibernate.ddl-auto=create-drop`).
- **Sécurité** : authentification par jeton JWT (pas de session, pas de
  cookie).
- **Documentation** : générée automatiquement (OpenAPI/Swagger UI).
- **Tests** : scénarios d'acceptation en français (Cucumber) + règles
  d'architecture automatisées (ArchUnit).

## 2. Prérequis et commandes

### Prérequis

- JDK 21 (testé avec Temurin 21.0.12).
- Maven 3.9+.

### Lancer les tests

```bash
mvn test
```

Compile le projet puis exécute l'intégralité des tests : les scénarios
Cucumber (démarrage réel de l'application sur un port aléatoire, appels HTTP
véritables) et les règles ArchUnit (analyse statique du bytecode compilé).
Au moment de l'écriture de ce README, la suite comporte 17 tests, tous
verts.

### Démarrer l'application

```bash
mvn spring-boot:run
```

Démarre l'API sur `http://localhost:8080` (port configuré dans
`application.yml`).

### Consulter la documentation interactive (Swagger UI)

Une fois l'application démarrée :

```
http://localhost:8080/swagger-ui.html
```

Les endpoints `/accounts/**` et `/transactions/**` y apparaissent comme
protégés (cadenas). Pour les essayer directement depuis Swagger UI :
1. Récupérer un jeton via `POST /auth/token` (voir ci-dessous) — utilisable
   aussi directement depuis l'interface, dans la section "Authentification".
2. Cliquer sur le bouton **Authorize** en haut de la page et coller le
   jeton (sans le préfixe `Bearer `, Swagger UI l'ajoute lui-même).

### S'authentifier

Un unique utilisateur de démonstration est créé en mémoire au démarrage :

- **Utilisateur** : `demo`
- **Mot de passe** : `demo123`

```bash
curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}'
```

Renvoie `{"token": "..."}` : un jeton JWT valable une heure, à transmettre
ensuite dans l'en-tête `Authorization: Bearer <jeton>` de chaque requête vers
`/accounts/**` ou `/transactions/**`.

## 3. Structure du projet

Le code de production (`src/main/java`) est organisé en couches, chacune ne
pouvant dépendre que de celle qui lui est immédiatement inférieure (règle
vérifiée automatiquement par les tests ArchUnit, voir section 6) :

```
com.hashtag.ngo.example.bank
├── entity/       Modèle persistant (JPA) et exceptions métier.
│                 Account (scellée), CheckingAccount, SavingsAccount,
│                 Transaction, AccountRepository, TransactionRepository,
│                 AccountNotFoundException, InsufficientFundsException,
│                 InvalidAmountException...
│
├── bean/         Contrats de la logique métier (interfaces uniquement) :
│                 AccountService, TransactionService, JwtService,
│                 et le record de commande TransactionCommand.
│   └── impl/     Implémentations de ces contrats : AccountServiceImpl,
│                 TransactionServiceImpl, JwtServiceImpl. Séparées de
│                 `bean` pour que le code appelant (contrôleurs, autres
│                 services) dépende toujours d'une interface, jamais
│                 d'une implémentation concrète.
│
└── api/          Couche REST : contrôleurs (AccountController,
                  TransactionController, AuthController), records de
                  requête/réponse (AccountRequest, AccountResponse...),
                  mappers MapStruct (AccountMapper, TransactionMapper),
                  sécurité (SecurityConfig, JwtAuthenticationFilter,
                  UserDetailsConfig), gestion des erreurs
                  (GlobalExceptionHandler) et configuration Swagger
                  (OpenApiConfig).
```

Chaque couche ne connaît que celle du dessous : `api` dépend de `bean` (via
les interfaces) et d'`entity`, `bean.impl` dépend d'`entity`, et `entity` ne
dépend de rien dans le projet. Aucune remontée n'est autorisée (par exemple
`entity` ne doit jamais importer une classe de `bean`).

Les tests (`src/test/java`) sont organisés séparément :

```
com.hashtag.ngo.example.bank
├── RunCucumberTest             Point d'entrée exécuté par Maven Surefire.
├── architecture/
│   └── ArchitectureTest        Règles ArchUnit (voir section 6).
└── cucumber/
    ├── CucumberSpringConfiguration   Démarre le contexte Spring pour les tests.
    ├── TestContext                  État partagé entre les steps d'un scénario.
    ├── AuthSteps, AccountSteps, TransactionSteps   Implémentation des steps.

src/test/resources/features/    Scénarios Gherkin (.feature), en français.
```

## 4. Fonctionnalités métier et nouveautés Java 21

Chaque fonctionnalité ci-dessous a été l'occasion d'utiliser une nouveauté
du langage introduite (ou stabilisée) en Java 21, plutôt qu'une écriture
plus classique :

| Fonctionnalité métier | Nouveauté Java 21 utilisée | Où la voir |
|---|---|---|
| Un compte est soit courant, soit épargne — jamais autre chose, et le compilateur le garantit | **Sealed classes** (`sealed`/`permits`) | `Account permits CheckingAccount, SavingsAccount` |
| Calculer le montant disponible sur un compte (le découvert n'existe que sur un compte courant) ; construire la réponse API adaptée au type de compte | **Pattern matching for switch** sur la hiérarchie scellée | `AccountServiceImpl.getAvailableBalance`, `AccountMapper.toResponse` |
| Appliquer un dépôt ou un retrait à partir d'une commande | **Record patterns** (déstructuration dans un `switch`) | `AccountServiceImpl.applyTransaction` |
| Traiter un lot de transactions sans épuiser les threads du serveur | **Threads virtuels** (`Executors.newVirtualThreadPerTaskExecutor()`) | `TransactionServiceImpl.processBatch` |
| Consulter l'historique d'un compte ; connaître sa toute dernière transaction | **Sequenced Collections** (`SequencedCollection`, `getLast()`, `reversed()`) | `TransactionServiceImpl.getHistory` / `getLastTransaction` |

## 5. Record vs DTO classique

Toutes les classes qui traversent la frontière de l'API (`AccountRequest`,
`AccountResponse`, `TransactionRequest`, `TransactionResponse`,
`TokenResponse`, `ErrorResponse`) ainsi que la commande interne
`TransactionCommand` sont des **records**, plutôt que des classes
classiques avec champs privés, getters, `equals`/`hashCode`/`toString`
écrits à la main (ou générés par un outil comme Lombok).

Pourquoi c'est un bon choix ici :

- **Immuabilité par défaut.** Un `AccountResponse` ne peut pas être modifié
  après sa création : une fois construit pour répondre à une requête HTTP,
  aucun code ne peut le faire changer "sous le pied" d'un autre. Pour un
  objet qui ne fait que transporter une donnée d'un point A à un point B,
  c'est exactement le comportement souhaité.
- **Zéro boilerplate.** `public record AccountResponse(Long id, String
  owner, ...)` donne gratuitement le constructeur, les accesseurs
  (`id()`, `owner()`...), `equals`/`hashCode` et `toString`. Une classe
  classique équivalente ferait quatre à cinq fois plus de lignes pour
  strictement la même information.
- **Comparaison par valeur.** Deux `TransactionCommand` portant les mêmes
  `accountId`/`type`/`amount` sont égaux (`equals`) sans rien écrire : très
  utile pour les tests.
- **Validation centralisée.** Le constructeur compact de
  `TransactionCommand` (`public TransactionCommand { ... }`) valide les
  champs obligatoires une seule fois, à la construction, pour tous les
  appelants — impossible de créer une commande invalide.
- **Se prêtent au pattern matching** décrit en section 4 (les *record
  patterns* ne fonctionnent que sur des records).

Ce choix ne s'applique **pas** aux entités JPA (`Account`, `Transaction`...),
qui restent des classes classiques : JPA a besoin d'un constructeur sans
argument, de champs mutables (pour appliquer les changements avant de les
persister) et, potentiellement, de générer des sous-classes proxy — autant
de choses incompatibles avec l'immuabilité d'un record. Les records sont
adaptés au *transport* de données (DTO), pas à la *persistance*.

## 6. Les briques techniques

### Cucumber — tests d'acceptation en langage naturel

Les scénarios (`src/test/resources/features/*.feature`) sont écrits en
français ("Étant donné... Quand... Alors..."), lisibles par quelqu'un qui ne
lit pas le code. Chaque étape est reliée à une méthode Java (`AuthSteps`,
`AccountSteps`, `TransactionSteps`) qui effectue de vrais appels HTTP contre
l'application démarrée sur un port aléatoire (`CucumberSpringConfiguration`,
`@SpringBootTest(webEnvironment = RANDOM_PORT)`). Ces appels utilisent
`RestClient` (le client HTTP synchrone de Spring 6+ ; `TestRestTemplate`,
utilisé dans les anciens projets Spring Boot, a été retiré en Spring Boot
4).

### MapStruct — conversion entité ↔ DTO sans réflexion

`AccountMapper` et `TransactionMapper` sont de simples interfaces annotées
`@Mapper(componentModel = "spring")` : à la compilation, MapStruct génère
l'implémentation (`AccountMapperImpl`, `TransactionMapperImpl`, visibles
dans `target/generated-sources`) qui recopie les champs un à un, sans
réflexion à l'exécution — donc sans coût de performance et avec des erreurs
de mapping détectées à la compilation plutôt qu'en production.

### ArchUnit — l'architecture vérifiée par un test

`ArchitectureTest` transforme les règles de conception du projet en tests
qui échouent si quelqu'un s'en écarte, même sans le vouloir :

- **Couches** : `api` peut dépendre de `bean` et `entity` ; `bean` (et donc
  `bean.impl`) ne peut dépendre que d'`entity` ; `entity` ne dépend de rien
  d'autre dans le projet. Aucune remontée n'est possible.
- **Nommage** : un contrôleur (`@RestController`) doit s'appeler `*Controller` ;
  une interface du package `bean` doit s'appeler `*Service` ; une classe du
  package `bean.impl` doit s'appeler `*ServiceImpl`.
- **Règle "impl"** : toute classe dont le nom se termine par `Impl` doit
  résider dans un package se terminant par `.impl`, et réciproquement,
  aucune interface ne doit résider dans un tel package. Cette règle connaît
  une seule exception assumée : les `*MapperImpl` générés par MapStruct
  (voir ci-dessus), qui suivent leur propre convention (à côté de
  l'interface qu'ils implémentent, pas dans un sous-package `.impl`) — ils
  sont exclus par leur nom, puisque leur annotation `@Generated` n'est pas
  visible dans le bytecode compilé (rétention `SOURCE`) et ne peut donc pas
  servir de critère à ArchUnit.

### Spring Security + JWT — une API sans état

`SecurityConfig` désactive les sessions et le CSRF (protection pensée pour
les cookies, inutile ici) et exige un jeton JWT valide pour tout appel à
`/accounts/**` ou `/transactions/**` ; `/auth/token`, `/swagger-ui/**` et
`/v3/api-docs/**` restent ouverts. `JwtAuthenticationFilter` lit l'en-tête
`Authorization: Bearer ...` de chaque requête, et, si le jeton est valide,
authentifie la requête auprès de Spring Security.

La génération et la validation des jetons eux-mêmes sont isolées dans
`JwtService` (interface dans `bean`, implémentation dans `bean.impl`) :
cette classe n'a **aucune dépendance à Spring MVC**, uniquement à la
bibliothèque JJWT. `AuthController` (couche `api`, pour émettre un jeton
après authentification) et `JwtAuthenticationFilter` (couche `api`, pour
valider un jeton entrant) réutilisent tous les deux le même `JwtService` :
la logique JWT n'est écrite qu'une fois, et resterait utilisable telle
quelle dans un contexte non-web (un traitement batch, par exemple).

### JPA / H2 — persistance et cas particulier des classes scellées

Les comptes sont persistés dans une unique table `accounts`, avec une
colonne discriminante (`@Inheritance(strategy = SINGLE_TABLE)`,
`@DiscriminatorColumn`) qui indique s'il s'agit d'un compte courant ou
épargne — c'est la stratégie d'héritage JPA la plus rapide en lecture
(aucune jointure).

`Account` étant une classe **scellée** (`sealed ... permits
CheckingAccount, SavingsAccount`), un problème se pose : par défaut,
Hibernate génère à l'exécution une sous-classe "proxy" de chaque entité
(via ByteBuddy) pour permettre le chargement différé (LAZY) — or une classe
scellée ne peut être étendue que par les classes listées dans son
`permits`, donc jamais par une classe générée dynamiquement. L'annotation
`@ConcreteProxy` (Hibernate 7) résout ce problème : au lieu de tenter de
générer un proxy de la classe abstraite scellée `Account`, Hibernate génère
un proxy par sous-classe concrète (`CheckingAccount`, `SavingsAccount`), ce
qui reste compatible avec le principe des classes scellées.

### Swagger UI — documentation générée, pas écrite à la main

`springdoc-openapi` lit les contrôleurs Spring MVC (annotations `@Tag`,
`@Operation`) et génère la spécification OpenAPI, exposée en JSON
(`/v3/api-docs`) et sous forme d'interface graphique interactive
(`/swagger-ui.html`) permettant d'essayer chaque endpoint sans écrire de
code. `OpenApiConfig` déclare les métadonnées générales de l'API et le
schéma de sécurité `bearerAuth`, référencé sur les contrôleurs protégés via
`@SecurityRequirement`.

## 7. Exemples d'appels curl

Récupérer un jeton et le stocker dans une variable :

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
```

Créer un compte courant :

```bash
curl -s -X POST http://localhost:8080/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "owner": "Alice",
        "type": "COURANT",
        "initialBalance": 1000.00,
        "overdraftLimit": 200.00
      }'
```

Répond `201 Created` (en-tête `Location: /accounts/{id}`) avec le compte
créé. Pour la suite des exemples, on suppose que son `id` vaut `1`.

Déposer de l'argent sur ce compte :

```bash
curl -s -X POST http://localhost:8080/accounts/1/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 150.00}'
```

Traiter un lot de transactions en une seule requête (chaque élément désigne
son propre compte ; traité en concurrence via des threads virtuels, voir
section 4) :

```bash
curl -s -X POST http://localhost:8080/transactions/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
        {"accountId": 1, "type": "DEPOT", "amount": 50.00},
        {"accountId": 1, "type": "RETRAIT", "amount": 30.00}
      ]'
```
