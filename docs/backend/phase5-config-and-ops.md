# Phase 5 — Configuration & Ops

[⬅ Phase 4](phase4-errors-and-validation.md) · [Index](README.md) · [Next: Phase 6 ➡](phase6-request-flow.md)

---

## `application.yml` — default + two profiles

| Profile | Database | `ddl-auto` | Flyway | Purpose |
|---------|----------|-----------|--------|---------|
| default | PostgreSQL (`jdbc:postgresql://localhost:5432/productdb`) | `validate` | on (`db/migration`) | normal run |
| `dev`   | PostgreSQL | `validate` | on + seed data (`db/dev-data`), `show-sql: true` | local dev |
| `test`  | H2 in-memory (PostgreSQL mode) | `create-drop` | off | fast unit/integration tests |

- Credentials use env vars with fallbacks: `${DB_USERNAME:postgres}`, `${DB_PASSWORD:postgres}`.
- Activate a profile with `-Dspring-boot.run.profiles=dev` or `SPRING_PROFILES_ACTIVE=dev`.

## Cross-cutting config — `config/`

- `WebConfig` — CORS: allows the Angular dev server (`http://localhost:4200`) to call the API.
- `OpenApiConfig` — Swagger UI (interactive API docs via springdoc-openapi).

## Build & Infra

- `pom.xml` — Spring Boot parent 3.3.4, Java 17, starters: `web`, `data-jpa`, `validation`,
  `flyway-core` + `flyway-database-postgresql`, `postgresql` driver, `lombok`, `springdoc`,
  plus test deps (`spring-boot-starter-test`, `h2`, `testcontainers`).
- `docker-compose.yml` — runs `postgres:16-alpine` (db `productdb`) on port 5432 for local dev.

## Run it locally

```bash
cd backend
docker compose up -d          # start PostgreSQL
./mvnw spring-boot:run        # start the API on :8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

[⬅ Phase 4](phase4-errors-and-validation.md) · [Index](README.md) · [Next: Phase 6 ➡](phase6-request-flow.md)
