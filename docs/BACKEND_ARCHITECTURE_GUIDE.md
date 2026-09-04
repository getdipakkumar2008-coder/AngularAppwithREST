# Spring Boot Backend Architecture Guide (Interview Prep)

> A mentor-style, file-by-file walkthrough of the `backend/` folder in this repo.
> Learn **how a single API request flows from the browser → Spring Boot → Hibernate → PostgreSQL** and back.
> Repo: `getdipakkumar2008-coder/AngularAppwithREST` · Stack: **Spring Boot 3.3 + Hibernate/JPA + Flyway + PostgreSQL + Java 17**

---

## Table of Contents
1. [The Big Picture](#1-the-big-picture)
2. [Folder & File Map](#2-folder--file-map)
3. [The Layered Architecture](#3-the-layered-architecture)
4. [File-by-File Explanation](#4-file-by-file-explanation)
5. [End-to-End Request Flow (the core section)](#5-end-to-end-request-flow)
6. [How Hibernate & PostgreSQL Fit In](#6-how-hibernate--postgresql-fit-in)
7. [Configuration & Environments](#7-configuration--environments)
8. [Interview Q&A by Phase](#8-interview-qa-by-phase)
9. [Glossary](#9-glossary)

---

## 1. The Big Picture

This backend is a **REST API for managing Products** (a classic CRUD app). It follows the standard
**layered / N-tier architecture** that almost every Spring Boot interview expects you to know.

```
Angular (browser :4200)
        │  HTTP JSON (GET/POST/PUT/DELETE /api/products)
        ▼
┌─────────────────────────────────────────────┐
│              SPRING BOOT APP (:8080)          │
│                                               │
│  Controller → Service → Repository            │
│      │           │           │                │
│    (DTO)      (business)   (JPA)              │
│      │           │           │                │
│   Mapper ◄───────┘        Hibernate           │
└───────────────────────────────────┬───────────┘
                                     │ SQL (JDBC)
                                     ▼
                            PostgreSQL (:5432)
                              table: product
```

**One-line summary you can say in an interview:**
> "A request hits the `@RestController`, which delegates to a `@Service` holding business logic. The service uses a Spring Data JPA `@Repository`, which Hibernate turns into SQL and runs against PostgreSQL over JDBC. Data crosses layers as DTOs and is converted to/from JPA entities by a mapper."

---

## 2. Folder & File Map

```
backend/
├── pom.xml                        # Maven build file — dependencies & plugins
├── docker-compose.yml             # Spins up a PostgreSQL container for local dev
├── .mvn/wrapper/                  # Maven Wrapper (run mvn without installing it)
└── src/
    ├── main/
    │   ├── java/com/example/productapp/
    │   │   ├── ProductAppApplication.java      # 🚀 Entry point (main method)
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java           # Swagger / OpenAPI docs setup
    │   │   │   └── WebConfig.java               # CORS config (allows Angular)
    │   │   ├── controller/
    │   │   │   └── ProductController.java       # 🌐 HTTP layer (routes)
    │   │   ├── service/
    │   │   │   ├── ProductService.java          # Business interface
    │   │   │   └── ProductServiceImpl.java      # Business logic + @Transactional
    │   │   ├── repository/
    │   │   │   └── ProductRepository.java       # 🗃️ Spring Data JPA interface
    │   │   ├── entity/
    │   │   │   └── Product.java                 # 🧱 JPA @Entity (maps to table)
    │   │   ├── dto/
    │   │   │   ├── ProductRequestDto.java       # Input shape + validation
    │   │   │   └── ProductResponseDto.java      # Output shape
    │   │   ├── mapper/
    │   │   │   └── ProductMapper.java           # Entity ⇄ DTO conversion
    │   │   └── exception/
    │   │       ├── ProductNotFoundException.java
    │   │       └── GlobalExceptionHandler.java  # Central error handling
    │   └── resources/
    │       ├── application.yml                  # ⚙️ Config (DB, JPA, profiles)
    │       └── db/
    │           ├── migration/V1__create_product_table.sql   # Flyway schema
    │           └── dev-data/V2__seed_products.sql           # Dev seed data
    └── test/java/.../productapp/
        ├── controller/ProductControllerTest.java
        ├── service/ProductServiceImplTest.java
        └── integration/ProductApiIntegrationTest.java
```

---

## 3. The Layered Architecture

Each layer has **one job** (Separation of Concerns). Requests flow **down**; data flows back **up**.

| Layer | Package | Responsibility | Key Annotation |
|-------|---------|----------------|----------------|
| **Presentation / Web** | `controller` | Receive HTTP, return HTTP. No business logic. | `@RestController` |
| **Business / Service** | `service` | Rules, orchestration, transactions. | `@Service`, `@Transactional` |
| **Data Access** | `repository` | Talk to the DB via JPA. | `@Repository` (implicit via `JpaRepository`) |
| **Domain / Persistence** | `entity` | Java object mapped to a DB table. | `@Entity` |
| **Transfer objects** | `dto` | Shape data crossing the API boundary. | `record` + `jakarta.validation` |
| **Cross-cutting** | `config`, `exception`, `mapper` | CORS, Swagger, error handling, conversions. | `@Configuration`, `@RestControllerAdvice` |

**Why DTOs instead of returning the entity directly?** (common interview question)
- Hides internal DB structure from clients.
- Prevents accidental exposure/mutation of fields.
- Lets input validation live separately from the persistence model.
- Avoids lazy-loading / serialization problems with Hibernate proxies.

---

## 4. File-by-File Explanation

### 🚀 `ProductAppApplication.java` — the entry point
```java
@SpringBootApplication
public class ProductAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductAppApplication.class, args);
    }
}
```
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- On startup Spring **scans** the `com.example.productapp` package, creates all beans
  (`@RestController`, `@Service`, `@Repository`, `@Component`, `@Configuration`), wires
  their dependencies (**Dependency Injection**), starts an **embedded Tomcat** server on port 8080,
  and runs **Flyway** migrations before the app is ready.

### 🌐 `controller/ProductController.java` — the web layer
- `@RestController` + `@RequestMapping("/api/products")` = base URL for all methods.
- Maps HTTP verbs to methods: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.
- `@PathVariable Long id` reads `/api/products/{id}`; `@RequestBody` deserializes JSON → DTO.
- `@Valid` triggers Bean Validation on the incoming DTO.
- Returns `ResponseEntity<...>` so it controls both **status code** and **body**.
- **Constructor injection** of `ProductService` (best practice — immutable, testable).

### ⚙️ Business layer: `service/ProductService.java` (interface) + `ProductServiceImpl.java`
- Interface defines the contract; impl holds the logic. (Coding to an interface = easy to mock/test/swap.)
- `@Service` marks it a Spring bean.
- `@Transactional` wraps each method in a DB transaction (commit on success, rollback on exception).
  Read methods use `@Transactional(readOnly = true)` — an optimization hint.
- Logic here: fetch all, find-by-id (or throw `ProductNotFoundException`), create, update, delete.
- Uses the **mapper** to translate between DTOs (API world) and entities (DB world).

### 🗃️ `repository/ProductRepository.java` — data access
```java
public interface ProductRepository extends JpaRepository<Product, Long> { }
```
- You write **zero SQL**. By extending `JpaRepository<Product, Long>` you inherit
  `findAll()`, `findById()`, `save()`, `deleteById()`, `existsById()`, etc.
- At runtime **Spring Data JPA generates the implementation**; Hibernate produces the actual SQL.
- `<Product, Long>` = entity type + primary key type.

### 🧱 `entity/Product.java` — the persistence model
- `@Entity` + `@Table(name = "product")` maps this class to the `product` table.
- `@Id` + `@GeneratedValue(strategy = IDENTITY)` → auto-increment primary key from Postgres.
- `@Column(...)` sets constraints (nullable, length, precision/scale for `BigDecimal price`).
- `@PrePersist` / `@PreUpdate` lifecycle hooks auto-set `createdDate` / `updatedDate`.
- Lombok (`@Getter/@Setter/@Builder/...`) removes boilerplate.

### 📦 `dto/` — the API contract
- `ProductRequestDto` (input): a Java `record` with validation — `@NotBlank`, `@NotNull`,
  `@Size`, `@PositiveOrZero`. If validation fails, the request never reaches the service.
- `ProductResponseDto` (output): what the client receives, including `id` and timestamps.

### 🔄 `mapper/ProductMapper.java`
- A `@Component` that converts `RequestDto → entity`, updates an entity in place, and
  converts `entity → ResponseDto`. Keeps conversion logic out of the service/controller.

### ❗ `exception/` — centralized error handling
- `ProductNotFoundException` — a custom `RuntimeException`.
- `GlobalExceptionHandler` — `@RestControllerAdvice` that catches exceptions app-wide and returns
  clean JSON with the right status: `404` (not found), `400` (validation failed, with field errors),
  `500` (unexpected). This is why controllers/services stay clean — no try/catch clutter.

### 🔧 `config/`
- `WebConfig` — CORS: allows the Angular dev server (`http://localhost:4200`) to call the API.
- `OpenApiConfig` — configures Swagger UI (interactive API docs via springdoc-openapi).

### 🗄️ `resources/db/` — Flyway migrations (schema as code)
- `V1__create_product_table.sql` creates the `product` table.
- `V2__seed_products.sql` (dev profile only) inserts sample rows.
- Flyway runs these **in version order at startup** and tracks them in a `flyway_schema_history` table,
  so every environment gets the **exact same schema**. This is why `ddl-auto` is set to `validate`
  (Hibernate checks the entity matches the schema instead of creating tables itself).

### `pom.xml` & `docker-compose.yml`
- `pom.xml`: Spring Boot parent 3.3.4, Java 17, and starters — `web`, `data-jpa`, `validation`,
  `flyway-core` + `flyway-database-postgresql`, `postgresql` driver, `lombok`, `springdoc`,
  plus test deps (`spring-boot-starter-test`, `h2`, `testcontainers`).
- `docker-compose.yml`: runs `postgres:16-alpine` with db `productdb` on port 5432 for local dev.

---

## 5. End-to-End Request Flow

Follow a **`POST /api/products`** (create a product) request, file by file. This is the exact
story to tell in an interview.

```
① CLIENT
   Angular sends:  POST http://localhost:8080/api/products
   Body: { "name": "Laptop", "price": 999.99, "quantity": 5 }

② EMBEDDED TOMCAT + SPRING DISPATCHERSERVLET
   - Tomcat receives the TCP request.
   - Spring's DispatcherServlet is the "front controller": it finds the handler
     that matches POST + /api/products.
   - CORS check happens here (WebConfig) — origin :4200 is allowed.

③ ProductController.createProduct(@Valid @RequestBody ProductRequestDto request)
   - Jackson deserializes JSON → ProductRequestDto.
   - @Valid runs Bean Validation. If invalid → MethodArgumentNotValidException
     → GlobalExceptionHandler returns 400 with field errors. (Flow stops here.)
   - If valid, controller calls: productService.createProduct(request)

④ ProductServiceImpl.createProduct(request)   [@Transactional opens a TX]
   - productMapper.toEntity(request)  →  builds a Product entity (no id yet).
   - productRepository.save(entity)   →  hands off to the data layer.

⑤ ProductRepository.save(...)  (Spring Data JPA proxy)
   - Delegates to Hibernate's EntityManager.persist().
   - @PrePersist fires → sets createdDate/updatedDate.

⑥ HIBERNATE (ORM)
   - Translates the entity into SQL:
       INSERT INTO product (name, description, price, quantity,
                            created_date, updated_date)
       VALUES (?, ?, ?, ?, ?, ?)
   - Binds parameters (prevents SQL injection).

⑦ JDBC → POSTGRESQL (:5432, db "productdb")
   - The PostgreSQL driver sends SQL over the connection (from the HikariCP pool).
   - Postgres inserts the row, returns the generated id.

⑧ RETURN TRIP (data flows back up)
   - Hibernate populates entity.id.
   - @Transactional commits the transaction.
   - Service: productMapper.toResponseDto(saved) → ProductResponseDto.
   - Controller: ResponseEntity.created(URI ".../{id}").body(dto)  → HTTP 201.
   - Jackson serializes DTO → JSON.

⑨ CLIENT receives:
   201 Created
   { "id": 1, "name": "Laptop", "price": 999.99, "quantity": 5,
     "createdDate": "...", "updatedDate": "..." }
```

**Compact flow diagram:**
```
JSON ─► DispatcherServlet ─► Controller ─► (validate/@Valid)
      ─► Service (@Transactional) ─► Mapper ─► Repository
      ─► Hibernate ─► JDBC ─► PostgreSQL
                    ◄──────── row + id ◄────────
      ◄─ Mapper→DTO ◄─ commit ◄─ ResponseEntity ◄─ JSON
```

**A GET /api/products/{id} that doesn't exist:**
`Controller → Service.findById → repo.findById → Optional empty → throw ProductNotFoundException → GlobalExceptionHandler → 404 JSON`.

---

## 6. How Hibernate & PostgreSQL Fit In

- **Hibernate** is the **ORM (Object-Relational Mapping)** engine — the reference implementation
  of the JPA specification. It maps the `Product` **Java object** ⇄ `product` **table row**.
- **Spring Data JPA** sits *on top of* Hibernate. You declare repository interfaces; it generates
  implementations and calls Hibernate for you.
- **JDBC** is the low-level Java DB API. Hibernate generates SQL and uses JDBC to run it.
- **HikariCP** (bundled with Spring Boot) is the **connection pool** — reuses DB connections
  instead of opening a new one per request (huge performance win).
- **PostgreSQL** is the actual relational database storing the data on disk.

Layer stack (top → bottom):
```
Your code → Spring Data JPA → Hibernate (JPA) → JDBC → HikariCP pool → PostgreSQL
```

**Why `ddl-auto: validate` + Flyway (not `create`/`update`)?**
- Flyway owns the schema (versioned SQL files). Hibernate only **validates** that entities match
  the tables. This is the production-safe pattern — predictable, reviewable, repeatable migrations.

---

## 7. Configuration & Environments

`application.yml` defines a **default** config plus two **profiles**:

| Profile | Database | `ddl-auto` | Flyway | Purpose |
|---------|----------|-----------|--------|---------|
| default | PostgreSQL (`jdbc:postgresql://localhost:5432/productdb`) | `validate` | on (`db/migration`) | normal run |
| `dev`   | PostgreSQL | `validate` | on + seed data (`db/dev-data`), `show-sql: true` | local dev |
| `test`  | H2 in-memory (`PostgreSQL` mode) | `create-drop` | off | fast unit/integration tests |

- Credentials come from env vars with fallbacks: `${DB_USERNAME:postgres}`, `${DB_PASSWORD:postgres}`.
- Activate a profile with `-Dspring-boot.run.profiles=dev` or `SPRING_PROFILES_ACTIVE=dev`.

**Run it locally:**
```bash
cd backend
docker compose up -d          # start PostgreSQL
./mvnw spring-boot:run        # start the API on :8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## 8. Interview Q&A by Phase

**Phase 1 — Fundamentals**
- *What is Spring Boot?* Opinionated framework on top of Spring that auto-configures beans,
  embeds a server, and minimizes boilerplate so you can run a production app with one `main`.
- *What does `@SpringBootApplication` do?* Combines `@Configuration`, `@EnableAutoConfiguration`,
  `@ComponentScan`.
- *IoC & Dependency Injection?* Spring creates and wires objects (beans) for you. This app uses
  **constructor injection** (immutable, test-friendly).

**Phase 2 — Layers**
- *Why separate Controller/Service/Repository?* Separation of concerns → testable, maintainable.
- *Controller vs Service?* Controller = HTTP concerns only; Service = business logic + transactions.
- *Why DTOs vs entities?* Decoupling, security, validation, avoids lazy-loading serialization issues.

**Phase 3 — Persistence**
- *JPA vs Hibernate vs Spring Data JPA?* JPA = spec; Hibernate = implementation; Spring Data JPA =
  abstraction that auto-generates repositories.
- *How does `JpaRepository` work with no code?* Spring creates a proxy implementing standard CRUD.
- *`@Transactional`?* Declarative transaction boundary; commit on success, rollback on runtime exception.
- *Custom finder?* Add `List<Product> findByName(String name);` — Spring derives the query from the name.

**Phase 4 — Data integrity & errors**
- *How is validation done?* `jakarta.validation` annotations on the DTO + `@Valid` in the controller.
- *Central error handling?* `@RestControllerAdvice` (`GlobalExceptionHandler`) → consistent JSON errors.
- *SQL injection?* Hibernate uses parameterized/prepared statements.

**Phase 5 — Ops**
- *Schema management?* Flyway versioned migrations; Hibernate set to `validate`.
- *Connection pooling?* HikariCP (default in Spring Boot).
- *Profiles?* `default`/`dev`/`test` for different DBs & behavior.

**Killer summary answer:**
> "Request → DispatcherServlet → `@RestController` (deserialize + validate) → `@Service`
> (`@Transactional` business logic) → Spring Data JPA `@Repository` → Hibernate generates SQL →
> JDBC over a HikariCP connection → PostgreSQL. The row comes back, Hibernate maps it to the entity,
> the mapper builds a response DTO, and the controller returns it as JSON with the right status code."

---

## 9. Glossary

| Term | Meaning |
|------|---------|
| **Bean** | An object managed by the Spring container. |
| **DI / IoC** | Dependency Injection / Inversion of Control — Spring wires objects for you. |
| **ORM** | Object-Relational Mapping — maps objects ⇄ table rows (Hibernate). |
| **JPA** | Jakarta Persistence API — the specification Hibernate implements. |
| **DTO** | Data Transfer Object — the shape of data crossing the API boundary. |
| **Entity** | A class mapped to a DB table via `@Entity`. |
| **DispatcherServlet** | Spring's front controller that routes HTTP requests to handlers. |
| **Flyway** | Tool that applies versioned SQL migrations to keep schemas in sync. |
| **HikariCP** | The default JDBC connection pool in Spring Boot. |
| **Embedded Tomcat** | The web server bundled inside the Spring Boot app. |

---

*Generated as a study guide for the `backend/` module of this repository. Read it top-to-bottom once, then use Section 5 and Section 8 to rehearse out loud before your interview. Good luck! 🚀*
