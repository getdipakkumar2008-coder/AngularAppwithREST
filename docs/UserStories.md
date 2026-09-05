# User Stories

**Domain assumption:** Requirement.txt does not name a business entity. As senior architect I am assuming a generic entity called **Product** (id, name, description, price, quantity, createdDate, updatedDate) to make the CRUD dashboard concrete. Rename/extend the entity later without changing the architecture.

## Epic: Product Dashboard CRUD

### US-1: View Dashboard List
**As a** user
**I want to** see a list of all products on a dashboard page
**So that** I can get an overview of existing data

**Acceptance Criteria:**
- Dashboard loads and calls `GET /api/products` on page load.
- Table/grid shows id, name, price, quantity (minimum columns).
- Empty state shown when list is empty.
- Loading indicator shown while fetching.
- Error message shown if API call fails.
- Pagination or basic client-side paging if list > 20 items (stretch).

### US-2: View Product Details
**As a** user
**I want to** click a row/item in the dashboard
**So that** I can navigate to a detail page for that specific product

**Acceptance Criteria:**
- Clicking a row navigates to `/products/:id`.
- Detail page calls `GET /api/products/{id}`.
- 404 / not-found state handled gracefully if id doesn't exist.

### US-3: Create Product
**As a** user
**I want to** add a new product via a form
**So that** new data can be entered into the system

**Acceptance Criteria:**
- "Add Product" button on dashboard opens create form (page or modal).
- Form validates required fields (name required, price >= 0, quantity >= 0) client-side.
- Submit calls `POST /api/products`.
- On success, redirect to dashboard/detail and show success message.
- On validation error from server (400), show field-level errors.

### US-4: Edit/Update Product
**As a** user
**I want to** edit an existing product's details from the detail page
**So that** I can keep the data current

**Acceptance Criteria:**
- Detail page has an "Edit" mode/button.
- Form pre-populated with existing values.
- Submit calls `PUT /api/products/{id}`.
- Optimistic-safe: server returns 404 if record was deleted concurrently.
- On success, show updated data and success message.

### US-5: Delete Product
**As a** user
**I want to** delete a product
**So that** stale/incorrect data can be removed

**Acceptance Criteria:**
- Delete action available from dashboard row and/or detail page.
- Confirmation dialog before delete is executed.
- Submit calls `DELETE /api/products/{id}`.
- On success, item removed from list / user redirected to dashboard.
- 404 handled gracefully if already deleted.

### US-6: API Data Persistence
**As a** system
**I want to** persist product data in PostgreSQL
**So that** data survives across sessions/restarts

**Acceptance Criteria:**
- Data stored via Spring Data JPA/Hibernate to PostgreSQL.
- Seed/dummy data loaded on startup (via `data.sql` or a `CommandLineRunner`) for demo purposes.
- Schema managed via Hibernate DDL or Flyway migration (decision in Architecture.md).

## Out of Scope (unless clarified later)
- Authentication/authorization
- Multi-user roles/permissions
- File uploads
- Search/filter/sort (beyond basic paging)
