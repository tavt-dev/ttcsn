# Friendify Monolith Runtime

This module is the modular monolith backend under package `com.friendify.app`.
It preserves the frontend-facing paths under `/api/v1/**` and the WebSocket
handshake endpoint `/ws`.

## Local Run

Prerequisites:

- Java 21
- Maven
- MySQL 8+
- Cloudinary account for media uploads
- Brevo API key for email delivery
- Google OAuth2 client credentials if OAuth2 login is enabled

Run from the repository root:

```powershell
cd monolith
copy .env.example .env
mvn spring-boot:run
```

Run tests:

```powershell
cd monolith
mvn test
mvn verify
```

## Required Environment

Use `monolith/.env.example` as the local template. Do not commit real secrets.

Core:

- `SERVER_PORT`
- `FRIENDIFY_DATASOURCE_URL`
- `FRIENDIFY_DATASOURCE_USERNAME`
- `FRIENDIFY_DATASOURCE_PASSWORD`
- `FRIENDIFY_JPA_DDL_AUTO`
- `FRIENDIFY_JPA_SHOW_SQL`
- `FRIENDIFY_CORS_ALLOWED_ORIGIN_PATTERNS`
- `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`

JWT:

- `FRIENDIFY_JWT_SIGNER_KEY`
- `FRIENDIFY_JWT_ISSUER`
- `FRIENDIFY_JWT_VALID_DURATION`
- `FRIENDIFY_JWT_REFRESHABLE_DURATION`

Email:

- `BREVO_URL`
- `BREVO_API_KEY`
- `BREVO_SENDER_NAME`
- `BREVO_SENDER_EMAIL`

Media:

- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

Google OAuth2:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `FRIENDIFY_OAUTH2_AUTHORIZED_REDIRECT_URI`
- `FRIENDIFY_OAUTH2_COOKIE_NAME`
- `FRIENDIFY_OAUTH2_COOKIE_SECURE`
- `FRIENDIFY_OAUTH2_COOKIE_SAME_SITE`
- `FRIENDIFY_OAUTH2_COOKIE_PATH`

Seed data:

- `FRIENDIFY_SEED_ENABLED`
- `FRIENDIFY_SEED_ADMIN_ENABLED`
- `FRIENDIFY_SEED_ADMIN_USERNAME`
- `FRIENDIFY_SEED_ADMIN_PASSWORD`
- `FRIENDIFY_SEED_ADMIN_EMAIL`

## Runtime Dependencies

The monolith runtime requires:

- MySQL
- Cloudinary
- Brevo
- Google OAuth2, if enabled

The monolith runtime does not require:

- `api-gateway`
- `config-server`
- Kafka
- Redis
- Feign clients for internal module calls
- MongoDB under the current MySQL-only target

## Docker Compose Profile

No repository-level docker-compose file existed when this cleanup was done.
If a compose file is added later, create a separate `monolith` profile that
starts MySQL and the monolith only. Do not include Kafka, Redis, config-server,
or api-gateway in the monolith profile unless a new runtime requirement is
explicitly approved.

Example service shape:

```yaml
services:
  mysql:
    image: mysql:8.4
    profiles: ["monolith"]
    environment:
      MYSQL_DATABASE: friendify_monolith
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    ports:
      - "3306:3306"

  friendify-monolith:
    profiles: ["monolith"]
    build:
      context: ./monolith
    env_file:
      - ./monolith/.env
    depends_on:
      - mysql
    ports:
      - "${SERVER_PORT:-8080}:8080"
```

Needs manual review: add a production Dockerfile or image build pipeline before
using the compose snippet directly.

## Rollback

Do not delete the legacy services yet. A safe rollback keeps the old
`api-gateway`, `config-server`, and microservice deployments available until the
monolith has passed endpoint parity and production smoke tests.

Rollback procedure:

1. Stop or remove the monolith instance from traffic.
2. Restore traffic to the existing `api-gateway`.
3. Confirm `config-server` is serving the old service configs.
4. Start the old microservices in the previous deployment order.
5. Point the frontend/API clients back to the gateway URL.
6. Verify auth, profile, media, post, interaction, notification, and chat smoke
   tests against the legacy stack.

Data rollback needs manual review because migrated modules currently target one
MySQL schema and several legacy services previously used MongoDB collections.
