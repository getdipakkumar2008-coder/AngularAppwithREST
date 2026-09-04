# Phase 4 — Validation & Error Handling

[⬅ Phase 3](phase3-persistence.md) · [Index](README.md) · [Next: Phase 5 ➡](phase5-config-and-ops.md)

---

## Input Validation

- `jakarta.validation` annotations live on the **request DTO**: `@NotBlank`, `@NotNull`,
  `@Size`, `@PositiveOrZero`.
- The controller uses `@Valid @RequestBody ProductRequestDto request`.
- If validation fails, the request **never reaches the service** — Spring throws
  `MethodArgumentNotValidException`.

## Centralized Error Handling — `exception/`

- `ProductNotFoundException` — a custom `RuntimeException` thrown by the service when an id
  doesn't exist.
- `GlobalExceptionHandler` — a `@RestControllerAdvice` that catches exceptions **app-wide** and
  returns clean JSON with the right status:

| Exception | HTTP Status | Body |
|-----------|-------------|------|
| `ProductNotFoundException` | `404 Not Found` | timestamp, status, error, message, path |
| `MethodArgumentNotValidException` | `400 Bad Request` | + `fieldErrors` map |
| any other `Exception` | `500 Internal Server Error` | generic message |

This is why controllers and services stay clean — no scattered `try/catch`.

## Why this matters

- **Consistent** error contract for the frontend to rely on.
- **Security** — you don't leak stack traces to clients.
- **Separation of concerns** — error formatting lives in one place.

[⬅ Phase 3](phase3-persistence.md) · [Index](README.md) · [Next: Phase 5 ➡](phase5-config-and-ops.md)
