# Product Dashboard CRUD App

Full-stack CRUD app: Angular frontend, Spring Boot + Hibernate backend, PostgreSQL database.

## Documentation

All planning/design docs live in [`docs/`](docs/):

- [`UserStories.md`](docs/UserStories.md) — user stories and acceptance criteria
- [`Architecture.md`](docs/Architecture.md) — system design, diagrams, key decisions
- [`Specification.md`](docs/Specification.md) — REST API contract, DTOs, validation rules
- [`Plan.md`](docs/Plan.md) — phased implementation plan
- [`Testing.md`](docs/Testing.md) — test strategy and coverage index

## Project layout

```
backend/    Spring Boot 3 API (Java 25, Spring Data JPA/Hibernate, Flyway, PostgreSQL)
frontend/   Angular 17 SPA (Reactive Forms, Router, HttpClient)
docs/       Architecture, specification, user stories, test strategy
```

## Running locally

### 1. Start PostgreSQL
```
cd backend
docker compose up -d
```

### 2. Start the backend (dev profile seeds sample data)
```
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
API available at http://localhost:8080/api/products

### 3. Start the frontend
```
cd frontend
npm install
ng serve
```
App available at http://localhost:4200/products (dev server proxies `/api` to `localhost:8080`, see `proxy.conf.json`)

## Running tests

```
# Backend
cd backend
mvn test              # unit tests
mvn verify             # includes Testcontainers integration test (requires Docker)

# Frontend
cd frontend
ng test --watch=false --browsers=ChromeHeadless
```
"# AngularAppwithREST" 
