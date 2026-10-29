# Hrayfi — Backend (Spring Boot 3, Java 21)

API REST + WebSocket de la plateforme Hrayfi : clients et artisans, annonces avec photos,
avis, recherche, messagerie avec négociation de prix, demandes d'intervention et notifications.

## Démarrer en local

1. Copier la configuration et lancer PostgreSQL :

```bash
cp .env.example .env
```

```bash
docker compose up -d postgres
```

2. Lancer l'API (Flyway crée le schéma au démarrage) :

```bash
./mvnw spring-boot:run
```

- API : `http://localhost:8080`
- Swagger UI : `http://localhost:8080/swagger-ui.html`
- Photos envoyées : dossier `UPLOAD_DIR` (par défaut `uploads/`), servies sous `/uploads/**`

`SEED_DEMO=true` crée des artisans, annonces et avis de démonstration
(`client.demo@hrayfi.ma` / `artisan.demo@hrayfi.ma`, mot de passe `password123`).

3. Test de bout en bout (l'API doit tourner) :

```bash
./scripts/smoke-test.sh
```

## Principaux endpoints

| Domaine | Endpoint | Accès |
| --- | --- | --- |
| Auth | `POST /api/auth/register/client`, `/register/artisan`, `/login` | public |
| Profil | `GET/PUT /api/users/me` | connecté |
| Artisans | `GET /api/artisans?metier=&ville=&q=`, `GET /api/artisans/{id}` | public |
| Annonces | `GET /api/annonces?q=&metier=&ville=&noteMin=&prixMax=&sort=` | public |
| | `GET /api/annonces/{id}`, `/metiers`, `/artisan/{id}` | public |
| | `POST /api/annonces` (multipart : `data` JSON + `images`) | artisan |
| | `PUT /api/annonces/{id}`, `PATCH /{id}/visibilite`, `DELETE /{id}` | artisan |
| | `POST /api/annonces/{id}/images`, `DELETE /{id}/images/{imageId}` | artisan |
| Avis | `GET /api/annonces/{id}/avis`, `GET /api/artisans/{id}/avis` | public |
| | `POST /api/annonces/{id}/avis` (crée ou modifie), `DELETE /api/avis/{id}`, `GET /api/avis/mes-avis` | client |
| Messages | `GET /api/messages/conversations`, `GET /conversation/{userId}` | connecté |
| | `POST /api/messages` (`prixPropose` + `annonceId` pour une offre) | connecté |
| | `PATCH /api/messages/conversation/{userId}/lu`, `GET /unread-count` | connecté |
| Demandes | `POST /api/demandes` (`annonceId`, `prixConvenu` optionnels), `GET /mes-demandes` | client |
| | `GET /api/demandes/recues`, `PATCH /{id}/accepter`, `/refuser`, `/terminer` | artisan |
| Dashboards | `GET /api/clients/me/dashboard`, `GET /api/artisans/me/dashboard` | client / artisan |
| Notifications | `GET /api/notifications`, `/unread-count`, `PATCH /{id}/lu`, `/mark-all-read` | connecté |

Temps réel (STOMP, endpoint `/ws`, en-tête `Authorization: Bearer <jwt>` au CONNECT) :
`/user/queue/messages` et `/user/queue/notifications`.

## Migrations

| Version | Contenu |
| --- | --- |
| V1 | utilisateurs (client / artisan), demandes, messages |
| V2 | évaluations des demandes, notifications |
| V3 | annonces et photos |
| V4 | avis sur les annonces |
| V5 | offres de prix et lecture des messages, lien demande ↔ annonce |
