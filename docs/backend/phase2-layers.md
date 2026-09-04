# Phase 2 — The Layered Architecture

[⬅ Phase 1](phase1-fundamentals.md) · [Index](README.md) · [Next: Phase 3 ➡](phase3-persistence.md)

---

Each layer has **one job** (Separation of Concerns). Requests flow **down**; data flows back **up**.

| Layer | Package | Responsibility | Key Annotation |
|-------|---------|----------------|----------------|
| **Presentation / Web** | `controller` | Receive HTTP, return HTTP. No business logic. | `@RestController` |
| **Business / Service** | `service` | Rules, orchestration, transactions. | `@Service`, `@Transactional` |
| **Data Access** | `repository` | Talk to the DB via JPA. | `@Repository` (implicit) |
| **Domain / Persistence** | `entity` | Java object mapped to a DB table. | `@Entity` |
| **Transfer objects** | `dto` | Shape data crossing the API boundary. | `record` + validation |
| **Cross-cutting** | `config`, `exception`, `mapper` | CORS, Swagger, errors, conversions. | `@Configuration`, `@RestControllerAdvice` |

---

## The Web Layer — `ProductController.java`

- `@RestController` + `@RequestMapping("/api/products")` = base URL for all methods.
- Maps HTTP verbs: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.
- `@PathVariable Long id` reads `/api/products/{id}`; `@RequestBody` deserializes JSON → DTO.
- `@Valid` triggers Bean Validation on the incoming DTO.
- Returns `ResponseEntity<...>` so it controls both **status code** and **body**.
- **Constructor injection** of `ProductService` (immutable, testable).

## The Business Layer — `ProductService` (interface) + `ProductServiceImpl`

- Interface defines the contract; impl holds the logic. Coding to an interface = easy to mock/swap.
- `@Service` marks it a Spring bean; `@Transactional` wraps each method in a DB transaction.
- Read methods use `@Transactional(readOnly = true)` — an optimization hint.
- Uses the **mapper** to translate between DTOs (API world) and entities (DB world).

## Transfer Objects — `dto/`

- `ProductRequestDto` (input): a `record` with validation (`@NotBlank`, `@NotNull`, `@Size`, `@PositiveOrZero`).
- `ProductResponseDto` (output): what the client receives, including `id` and timestamps.

**Why DTOs instead of returning the entity directly?** (classic interview question)
- Hides internal DB structure from clients.
- Prevents accidental exposure/mutation of fields.
- Keeps input validation separate from the persistence model.
- Avoids lazy-loading / serialization problems with Hibernate proxies.

## The Mapper — `ProductMapper.java`

A `@Component` that converts `RequestDto → entity`, updates an entity in place, and converts
`entity → ResponseDto`. Keeps conversion logic out of the controller/service.

[⬅ Phase 1](phase1-fundamentals.md) · [Index](README.md) · [Next: Phase 3 ➡](phase3-persistence.md)
