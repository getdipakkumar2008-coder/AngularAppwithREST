# Specification.md

## 1. Entity: Product

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-generated |
| name | String | required, 1–255 chars |
| description | String | optional, max 1000 chars |
| price | BigDecimal | required, >= 0, scale 2 |
| quantity | Integer | required, >= 0 |
| createdDate | Instant | server-set, immutable after create |
| updatedDate | Instant | server-set, updated on every write |

## 2. REST API Specification

Base path: `/api/products`

### GET /api/products
- 200 OK → `ProductResponseDto[]`
- Empty DB → 200 OK with `[]`

### GET /api/products/{id}
- 200 OK → `ProductResponseDto`
- 404 Not Found → error body if id doesn't exist

### POST /api/products
Request body:
```json
{ "name": "string", "description": "string", "price": 0.00, "quantity": 0 }
```
- 201 Created → `ProductResponseDto` + `Location` header `/api/products/{id}`
- 400 Bad Request → validation errors, e.g.:
```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "name": "must not be blank", "price": "must be greater than or equal to 0" } }
```

### PUT /api/products/{id}
Request body: same shape as POST.
- 200 OK → updated `ProductResponseDto`
- 404 Not Found → id doesn't exist
- 400 Bad Request → validation errors

### DELETE /api/products/{id}
- 204 No Content → deleted
- 404 Not Found → id doesn't exist

### ProductResponseDto
```json
{
  "id": 1,
  "name": "Sample Product",
  "description": "A sample product",
  "price": 19.99,
  "quantity": 5,
  "createdDate": "2026-09-03T10:00:00Z",
  "updatedDate": "2026-09-03T10:00:00Z"
}
```

### Standard error body (all non-2xx)
```json
{
  "timestamp": "2026-09-03T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 5 not found",
  "path": "/api/products/5"
}
```

## 3. Frontend Route/Component Spec

| Route | Component | Behavior |
|---|---|---|
| `/products` | DashboardComponent | Fetch & list all products, link each row to `/products/:id`, delete button per row (with confirm) |
| `/products/new` | ProductFormComponent | Empty form → POST → navigate to `/products/:id` on success |
| `/products/:id` | ProductDetailComponent | Fetch product by id, show fields, Edit/Delete buttons |
| `/products/:id/edit` | ProductFormComponent | Pre-filled form → PUT → navigate to `/products/:id` on success |

## 4. Validation Rules (mirrored client + server)
- `name`: required, non-blank, ≤255 chars
- `price`: required, numeric, ≥ 0
- `quantity`: required, integer, ≥ 0
- `description`: optional, ≤1000 chars

## 5. Non-functional Requirements
- API responses < 500ms for list of ≤1000 rows (local/dev).
- Frontend must show loading/error/empty states for every API-backed view.
- Backend must return consistent JSON error shape for all 4xx/5xx.
- CORS restricted to known frontend origin(s), not `*`.
