# Phase 1 — Fundamentals

[⬅ Index](README.md) · [Next: Phase 2 ➡](phase2-layers.md)

---

## The Big Picture

This backend is a **REST API for managing Products** (a classic CRUD app) using the standard
**layered / N-tier architecture** that almost every Spring Boot interview expects you to know.

```
Angular (browser :4200)
        │  HTTP JSON (GET/POST/PUT/DELETE /api/products)
        ▼
┌─────────────────────────────────────────────┐
│              SPRING BOOT APP (:8080)          │
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

**One-line summary for an interview:**
> "A request hits the `@RestController`, which delegates to a `@Service` holding business logic. The service uses a Spring Data JPA `@Repository`, which Hibernate turns into SQL and runs against PostgreSQL over JDBC. Data crosses layers as DTOs and is converted to/from JPA entities by a mapper."

---

## The Entry Point — `ProductAppApplication.java`

```java
@SpringBootApplication
public class ProductAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductAppApplication.class, args);
    }
}
```

- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- On startup Spring **scans** `com.example.productapp`, creates all beans
  (`@RestController`, `@Service`, `@Repository`, `@Component`, `@Configuration`), wires their
  dependencies (**Dependency Injection**), starts an **embedded Tomcat** on port 8080, and runs
  **Flyway** migrations before the app is ready.

---

## Core Concepts

- **Spring Boot** — opinionated framework that auto-configures beans, embeds a server, and
  minimizes boilerplate so you can run a production app from one `main`.
- **IoC / Dependency Injection** — Spring creates and wires objects (beans) for you. This app
  uses **constructor injection** (immutable, test-friendly).
- **Bean** — any object managed by the Spring container.

[⬅ Index](README.md) · [Next: Phase 2 ➡](phase2-layers.md)
