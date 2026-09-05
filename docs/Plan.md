# Plan.md

## Phase 0 — Documentation (this phase)
- [x] UserStories.md
- [x] Architecture.md
- [x] Specification.md
- [x] Plan.md
- [x] Backend unit test skeletons
- [x] Backend integration test skeletons
- [x] Frontend unit test skeletons
- [x] Frontend integration/e2e-lite test skeletons

## Phase 1 — Backend Scaffolding
1. Generate Spring Boot project (Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Flyway) via Spring Initializr / start.spring.io equivalent.
2. Configure `application.yml` (dev/test profiles) + Docker Compose for local PostgreSQL.
3. Create `Product` entity + Flyway `V1__create_product_table.sql`.
4. Create `ProductRepository`.
5. Create DTOs (`ProductRequestDto`, `ProductResponseDto`) + mapper.
6. Create `ProductService`/`ProductServiceImpl` with CRUD methods.
7. Create `ProductController` with 5 REST endpoints.
8. Create `GlobalExceptionHandler` + `ProductNotFoundException`.
9. Add CORS config for `http://localhost:4200`.
10. Seed dummy data (Flyway `V2__seed_products.sql`).
11. Run unit + integration tests (written in Phase 0) against implementation, fix until green.

## Phase 2 — Frontend Scaffolding
1. Generate Angular app (`ng new`), add routing, HttpClientModule.
2. Create `Product` interface/model.
3. Create `ProductService` (HttpClient calls to backend).
4. Create `DashboardComponent` (list + delete + navigate).
5. Create `ProductDetailComponent`.
6. Create `ProductFormComponent` (shared create/edit, Reactive Forms).
7. Wire routing module.
8. Add loading/error/empty states.
9. Add environment config for API base URL.
10. Run unit tests (written in Phase 0), fix until green.

## Phase 3 — Integration & Verification
1. Run backend + frontend together locally (`docker-compose up` for Postgres, `mvn spring-boot:run`, `ng serve`).
2. Manually verify all 5 user stories end-to-end.
3. Run full test suite (backend + frontend) and confirm green.
4. Review CORS, validation, and error handling behavior against Specification.md.

## Phase 4 — Polish (stretch, only if time allows)
- Pagination/sorting on dashboard.
- Basic search/filter by name.
- OpenAPI/Swagger UI for backend.
- Dockerfiles for both apps + docker-compose for full stack.

## Sequencing Rationale
Backend is built and tested first (Phase 1) since the frontend depends on a stable, verified API contract (Specification.md). Tests are written before implementation (Phase 0) per TDD-style approach requested by the user — implementation in Phases 1–2 should make these tests pass, not the reverse.
