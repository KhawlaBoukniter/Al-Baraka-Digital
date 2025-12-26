# Al Baraka Digital - Plateforme bancaire sécurisée

**Code source sur GitHub** : [https://github.com/votre-username/al-baraka-digital](https://github.com/votre-username/al-baraka-digital) *(remplacez par votre lien réel)*

Projet backend Spring Boot développé en **respect strict** des exigences du brief fourni.

## Contexte

La banque **Al Baraka Digital** souhaite digitaliser la gestion de ses opérations bancaires (dépôts, retraits, virements) pour ses clients et agents internes.  
Les opérations doivent être sécurisées, traçables et conformes aux règles internes de validation pour les montants importants (> 10 000 DH).

### Problèmes identifiés
- Opérations sensibles traitées manuellement
- Risque de fraude ou d’erreurs sur les opérations importantes
- Difficulté de traçabilité et de contrôle interne
- Absence d’automatisation sécurisée pour les comptes clients et agents

### Objectifs atteints
- Sécurisation des accès aux APIs via **JWT stateless**
- Gestion complète de la logique métier (dépôts, retraits, virements)
- Workflows de validation selon le montant
- Déploiement dans un conteneur Docker avec Docker Compose
- Base prête pour une future intégration CI/CD

## Fonctionnalités implémentées

- Création de compte client avec génération automatique d’un numéro de compte unique
- Authentification JWT stateless pour tous les rôles
- Opérations bancaires avec workflow :
    - ≤ 10 000 DH → exécution automatique
    - > 10 000 DH → statut **PENDING** + upload obligatoire de justificatif (PDF/JPG/PNG, max 5MB)
- Consultation des opérations PENDING par l’agent via **OAuth2** (scope `operations.read`)
- Approbation / rejet des opérations PENDING par l’agent (JWT + rôle AGENT_BANCAIRE)
- Gestion complète des utilisateurs par l’admin (création, modification, suppression, activation)
- Liste des opérations personnelles pour le client
- Gestion claire des erreurs (token expiré, invalide, accès refusé, etc.)
- Évitement des boucles infinies JSON via `@JsonIgnoreProperties`

## Sécurité & JWT

### Architecture de sécurité
- **JWT stateless** pour la majorité des endpoints
- **OAuth2 Resource Server** uniquement pour `/api/agent/operations/pending` (scope `operations.read`)
- Deux `SecurityFilterChain` séparées :
    - Une pour OAuth2 (pending)
    - Une pour JWT (tout le reste)
- Filtre `JwtAuthenticationFilter` exclusif pour les endpoints non-OAuth2
- Spring Security 6 + `UserDetailsService` personnalisé
- Hashage des mots de passe avec **BCrypt**

### Flux JWT
1. Client envoie email + mot de passe à `/auth/login`
2. `AuthenticationManager` valide les credentials
3. `JwtUtils` génère un JWT signé
4. Client utilise le JWT dans l’en-tête `Authorization: Bearer <token>`
5. `JwtAuthenticationFilter` valide le token et place l’utilisateur dans le `SecurityContext`
6. Accès autorisé selon le rôle

### Flux Security Filter Chain
- Requête → `JwtAuthenticationFilter` (sauf pour pending)
- Si token valide → authentification réussie
- Sinon → rejet 401
- Pour `/api/agent/operations/pending` → traité par le filtre OAuth2 Resource Server (validation via Keycloak)

## Documentation des endpoints

| Endpoint                              | Méthode | Accès / Rôle                  | Description                                      |
|---------------------------------------|---------|-------------------------------|--------------------------------------------------|
| `/auth/login`                         | POST    | Public                        | Authentification → retourne JWT                  |
| `/api/client/register`                | POST    | Public                        | Création de compte client                        |
| `/api/client/operations`              | POST    | CLIENT                        | Créer opération (dépôt/retrait/virement)         |
| `/api/client/operations/{id}/document`| POST    | CLIENT                        | Upload justificatif (multipart/form-data)        |
| `/api/client/operations`              | GET     | CLIENT                        | Lister ses opérations                            |
| `/api/agent/operations/pending`       | GET     | AGENT_BANCAIRE (OAuth2)       | Lister toutes les opérations PENDING             |
| `/api/agent/operations/{id}/approve`  | PUT     | AGENT_BANCAIRE                | Approuver une opération PENDING                  |
| `/api/agent/operations/{id}/reject`   | PUT     | AGENT_BANCAIRE                | Rejeter une opération PENDING                    |
| `/api/admin/users`                    | POST    | ADMIN                         | Créer un utilisateur (client/agent/admin)        |
| `/api/admin/users/{id}`               | PUT     | ADMIN                         | Modifier un utilisateur                          |
| `/api/admin/users/{id}`               | DELETE  | ADMIN                         | Supprimer un utilisateur                         |

## Docker & Déploiement

Le projet est entièrement dockerisé avec **Docker Compose** incluant :

- Backend Spring Boot
- Base de données PostgreSQL
- Keycloak (serveur OAuth2)

### Lancement
```bash
docker-compose up --build
```

### Services

- Backend → http://localhost:8080
- PostgreSQL → port 5432
- Keycloak → http://localhost:8180 (admin / admin)

Un realm albaraka doit être importé dans Keycloak (dossier ./keycloak-imports recommandé).

## Technologies

* Java 17
* Spring Boot 3.2+
* Spring Security 6 + OAuth2 Resource Server
* Spring Data JPA
* PostgreSQL
* Maven
* Lombok
* Keycloak 24
* Docker & Docker Compose

_**Al Baraka Digital** – Une plateforme bancaire moderne, sécurisée et entièrement dockerisée._