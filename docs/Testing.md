# Testing.md — Test Strategy & Index

These tests are written **before implementation** (TDD-style, per Plan.md). They currently reference classes/components that don't exist yet — that's expected; Phase 1/2 implementation should make them pass.

## Backend (`backend/src/test/java/...`)
| File | Type | Covers |
|---|---|---|
| `service/ProductServiceImplTest.java` | Unit (Mockito) | CRUD business logic in isolation, not-found exceptions |
| `controller/ProductControllerTest.java` | Unit (`@WebMvcTest`) | HTTP status codes, request validation, JSON shape, service mocked |
| `integration/ProductApiIntegrationTest.java` | Integration (`@SpringBootTest` + Testcontainers PostgreSQL) | Full CRUD lifecycle through real HTTP + real DB |

Run: `mvn test` (unit) and `mvn verify` (includes integration tests, requires Docker for Testcontainers).

## Frontend (`frontend/src/app/...`)
| File | Type | Covers |
|---|---|---|
| `services/product.service.spec.ts` | Unit (`HttpClientTestingModule`) | Service methods map to correct HTTP verbs/URLs, error propagation |
| `dashboard/dashboard.component.spec.ts` | Unit | Load/loading/error/empty states, navigation, delete |
| `product-form/product-form.component.spec.ts` | Unit | Reactive form validation, create-mode submit flow |
| `integration/product-flow.integration.spec.ts` | Integration | Real Router + real HttpClient (mocked at HTTP layer) across Dashboard → Detail → Edit |

Run: `ng test`.

## Coverage vs. User Stories
- US-1 (view list) → `DashboardComponent` unit tests + integration test's empty-state case + backend `getAllProducts`/`GET /api/products` tests.
- US-2 (view detail) → integration test's dashboard→detail navigation + backend `getProductById`/`GET /{id}` tests.
- US-3 (create) → `ProductFormComponent` unit tests + backend `createProduct`/`POST` tests (valid + validation-error cases).
- US-4 (update) → backend `updateProduct`/`PUT` tests + integration lifecycle test's update step.
- US-5 (delete) → `DashboardComponent` delete test + backend `deleteProduct`/`DELETE` tests (existing + already-deleted).
- US-6 (persistence) → `ProductApiIntegrationTest` (real PostgreSQL via Testcontainers).

## Not Yet Covered (add once implemented)
- E2E browser test (Cypress/Playwright) for full click-through across real backend.
- Contract test between Angular `Product` model and backend DTO (could use a shared OpenAPI schema).
