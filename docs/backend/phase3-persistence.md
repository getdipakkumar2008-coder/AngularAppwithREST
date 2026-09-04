# Phase 3 — Persistence (Hibernate + PostgreSQL)

[⬅ Phase 2](phase2-layers.md) · [Index](README.md) · [Next: Phase 4 ➡](phase4-errors-and-validation.md)

---

## The Persistence Model — `entity/Product.java`

- `@Entity` + `@Table(name = "product")` maps this class to the `product` table.
- `@Id` + `@GeneratedValue(strategy = IDENTITY)` → auto-increment primary key from Postgres.
- `@Column(...)` sets constraints (nullable, length, precision/scale for `BigDecimal price`).
- `@PrePersist` / `@PreUpdate` lifecycle hooks auto-set `createdDate` / `updatedDate`.
- Lombok (`@Getter/@Setter/@Builder/...`) removes boilerplate.

## The Data Access Layer — `ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> { }
```

- You write **zero SQL**. Extending `JpaRepository<Product, Long>` gives you `findAll()`,
  `findById()`, `save()`, `deleteById()`, `existsById()`, etc.
- At runtime **Spring Data JPA generates the implementation**; Hibernate produces the SQL.
- `<Product, Long>` = entity type + primary key type.
- Add a custom finder like `List<Product> findByName(String name);` and Spring derives the query
  from the method name.

## The Layer Stack

```
Your code → Spring Data JPA → Hibernate (JPA) → JDBC → HikariCP pool → PostgreSQL
```

- **Hibernate** = the ORM engine; reference implementation of the **JPA** spec. Maps the `Product`
  object ⇄ `product` row.
- **Spring Data JPA** sits on top of Hibernate; you declare interfaces, it generates implementations.
- **JDBC** = the low-level Java DB API Hibernate uses to run SQL.
- **HikariCP** = the default connection pool — reuses DB connections instead of opening one per request.
- **PostgreSQL** = the actual relational database.

## Transactions

- `@Transactional` (on the service) opens a transaction, commits on success, **rolls back** on a
  runtime exception.
- SQL is **parameterized** by Hibernate → protects against SQL injection.

## Schema Management — `resources/db/`

- `V1__create_product_table.sql` creates the `product` table.
- `V2__seed_products.sql` (dev only) inserts sample rows.
- **Flyway** runs these in version order at startup and tracks them in `flyway_schema_history`,
  so every environment gets the exact same schema.

**Why `ddl-auto: validate` + Flyway (not `create`/`update`)?** Flyway owns the schema; Hibernate
only **validates** that entities match the tables — the production-safe pattern.

[⬅ Phase 2](phase2-layers.md) · [Index](README.md) · [Next: Phase 4 ➡](phase4-errors-and-validation.md)
