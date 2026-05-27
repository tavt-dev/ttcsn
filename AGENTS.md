# AGENTS.md

## Project Goal

Migrate Friendify from a Spring microservice backend into a modular monolith.

The target is one deployable backend application, while keeping code separated by domain modules. Do not turn the codebase into one large package.

## Current System

Existing deployable services:

- `api-gateway`
- `config-server`
- `identity-service`
- `profile-service`
- `post-service`
- `interaction-service`
- `social-service`
- `group-service`
- `chat-service`
- `file-service`
- `notification-service`

Existing shared libraries:

- `shared-common`
- `shared-contacts`

Current important infrastructure:

- MySQL: identity, profile, social, interaction.
- MongoDB: post, group, chat, file, notification.
- Kafka: used by legacy services for notification delivery, image upload request/reply,
  and interaction/post/user events. Kafka is not required for the target monolith;
  remove it during migration when direct application calls or in-process events are enough.
- Cloudinary: media storage.
- Brevo email API is used in code. README mentions SendGrid, but code is the source of truth unless explicitly changed.
- Redis is mentioned in README, but no real Redis usage was found in source/config. Do not add Redis unless it is actually needed for cache, session, rate-limit, or token blacklist.

## Monolith Location And Package

Use the `monolith/` directory for the new application.

Prefer this package root because the current monolith skeleton uses it:

```text
src/main/java/com/friendify/app
```

Target modular package layout:

```text
com.friendify.app
  /auth
  /profile
  /post
  /interaction
  /social
  /group
  /chat
  /file
  /notification
  /shared
  /config
```

Do not use generic sample domains like `product`, `order`, or `payment`; they are not part of Friendify.

## Version Policy

Before migrating code, align the monolith build with the existing services unless the user explicitly asks for an upgrade:

- Java 21
- Spring Boot 4.0.6
- Spring Cloud version used by existing services where still needed
- Maven

## Migration Rules

- Read the source service before moving its code.
- Make small, reviewable changes.
- Preserve existing business logic.
- Preserve existing REST API contracts unless explicitly asked.
- Preserve gateway-visible paths under `/api/v1/{domain}/...` when exposing monolith endpoints.
- Keep internal endpoints only as temporary compatibility adapters; prefer direct application service calls inside the monolith.
- Remove service-to-service HTTP calls by replacing Feign/WebClient/RestTemplate clients with direct application service ports.
- Prefer removing Kafka during migration unless a real external integration still depends on it.
- Replace Kafka with direct application service calls or in-process domain events when the
  behavior is still needed inside the monolith.
- Remove Kafka producers/listeners/config after confirming the monolith no longer needs that
  asynchronous boundary.
- Do not delete old services until the equivalent monolith module builds and tests pass.
- Always run build/tests after each major migration slice.
- Explain every risky change before or while making it.
- Do not rewrite the whole project at once.

## Safe Migration Order

1. Stabilize `monolith/` shell and align versions/package.
2. Move shared contracts/utilities:
   - response wrappers
   - paging wrapper
   - exception model
   - media contracts
   - notification event contract
   - security/OpenAPI common config
3. Migrate `identity-service` and `profile-service` together.
4. Migrate `file-service` and replace Kafka image upload request/reply with direct file application service calls.
5. Migrate `social-service`.
6. Migrate `group-service`.
7. Migrate `interaction-service`.
8. Migrate `post-service`.
9. Migrate `notification-service`.
10. Migrate `chat-service`.
11. Retire `api-gateway` and `config-server` only after monolith API parity is verified.

## Database Rules

- First monolith version may keep MySQL and MongoDB side-by-side.
- Do not merge MongoDB document models into MySQL during the first migration pass.
- Prefer one MySQL schema for JPA modules, but create a migration plan before moving existing service schemas.
- Review table names and indexes before production migration.
- Be careful with cross-store workflows, especially post deletion affecting MongoDB posts and MySQL interactions.

## Testing Rules

After each migration slice, run tests from `monolith/`.

At minimum add or preserve tests for:

- application context loading
- registration creates user and profile
- auth token lifecycle
- profile search/update
- image upload after file module migration
- social block/friend/follow queries used by feed
- group can-view/can-post checks
- post delete cleans comments/likes
- notification email dispatch after notification module migration
- chat WebSocket auth after chat migration

## Manual Review Risks

- Gateway path compatibility can break clients.
- Security behavior is duplicated across old services and must be unified carefully.
- Kafka request/reply image upload changes failure timing when replaced by direct calls.
- Notification behavior must remain reliable after replacing Kafka.
- Existing tests are thin, so add focused tests before deleting old services.
- README contains stale/incorrect details, including Redis and SendGrid references.
