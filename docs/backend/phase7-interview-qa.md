# Phase 7 — Interview Q&A + Glossary

[⬅ Phase 6](phase6-request-flow.md) · [Index](README.md)

---

## Q&A by phase

**Fundamentals**
- *What is Spring Boot?* Opinionated framework on top of Spring that auto-configures beans,
  embeds a server, and minimizes boilerplate.
- *What does `@SpringBootApplication` do?* Combines `@Configuration`, `@EnableAutoConfiguration`,
  `@ComponentScan`.
- *IoC & DI?* Spring creates and wires beans; this app uses constructor injection.

**Layers**
- *Why separate Controller/Service/Repository?* Separation of concerns → testable, maintainable.
- *Controller vs Service?* Controller = HTTP only; Service = business logic + transactions.
- *Why DTOs vs entities?* Decoupling, security, validation, avoids lazy-loading serialization issues.

**Persistence**
- *JPA vs Hibernate vs Spring Data JPA?* JPA = spec; Hibernate = implementation; Spring Data JPA =
  abstraction that auto-generates repositories.
- *How does `JpaRepository` work with no code?* Spring creates a proxy implementing standard CRUD.
- *`@Transactional`?* Declarative transaction boundary; commit on success, rollback on runtime exception.
- *Custom finder?* `List<Product> findByName(String name);` — Spring derives the query from the name.

**Data integrity & errors**
- *Validation?* `jakarta.validation` annotations on the DTO + `@Valid` in the controller.
- *Central error handling?* `@RestControllerAdvice` (`GlobalExceptionHandler`) → consistent JSON errors.
- *SQL injection?* Hibernate uses parameterized/prepared statements.

**Ops**
- *Schema management?* Flyway versioned migrations; Hibernate set to `validate`.
- *Connection pooling?* HikariCP (default in Spring Boot).
- *Profiles?* `default`/`dev`/`test` for different DBs & behavior.

## Killer summary answer

> "Request → DispatcherServlet → `@RestController` (deserialize + validate) → `@Service`
> (`@Transactional` business logic) → Spring Data JPA `@Repository` → Hibernate generates SQL →
> JDBC over a HikariCP connection → PostgreSQL. The row comes back, Hibernate maps it to the entity,
> the mapper builds a response DTO, and the controller returns it as JSON with the right status code."

## Glossary

| Term | Meaning |
|------|---------|
| **Bean** | An object managed by the Spring container. |
| **DI / IoC** | Dependency Injection / Inversion of Control. |
| **ORM** | Object-Relational Mapping (Hibernate). |
| **JPA** | Jakarta Persistence API — the spec Hibernate implements. |
| **DTO** | Data Transfer Object — shape of data crossing the API boundary. |
| **Entity** | A class mapped to a DB table via `@Entity`. |
| **DispatcherServlet** | Spring's front controller that routes HTTP requests. |
| **Flyway** | Applies versioned SQL migrations to keep schemas in sync. |
| **HikariCP** | The default JDBC connection pool in Spring Boot. |
| **Embedded Tomcat** | The web server bundled inside the Spring Boot app. |

[⬅ Phase 6](phase6-request-flow.md) · [Index](README.md)
