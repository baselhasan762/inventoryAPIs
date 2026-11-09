# Inventory API

A REST API for tracking products and stock across several warehouses, built with Spring Boot 4.
Every stock change is recorded as a movement, transfers between locations are all-or-nothing,
and access is secured with JWT access tokens and rotating refresh tokens.

## Features

- **Products, categories and locations** with soft delete (products and locations can be reactivated)
- **Stock levels** per product and location, kept in sync with every movement in one transaction
- **Stock movements**: stock in, stock out, and transfers between locations recorded as a linked
  `TRANSFER_OUT` / `TRANSFER_IN` pair
- **Low-stock report**: products whose total quantity is at or below their reorder level
- **Search, filtering and pagination** on every list endpoint (max 100 rows per page)
- **JWT authentication** with 15-minute access tokens and 7-day refresh tokens
- **Role-based access**: `ADMIN` and `STAFF`, plus ownership rules (staff can edit products they created)
- **Consistent errors**: every error is an RFC 9457 `ProblemDetail` with a readable message
- **Interactive docs** with Swagger UI

## Tech stack

Java 21 · Spring Boot 4.1 · Spring Security 7 · Spring Data JPA / Hibernate 7 · MySQL ·
Flyway · jjwt · springdoc-openapi · Lombok · Maven · Docker · JUnit 6 · MockMvc · Testcontainers

## Getting started

### Run with Docker (recommended)

The only requirement is Docker. Compose starts the app and a MySQL database,
and Flyway creates the tables on first start.

```bash
cp .env.example .env    # then fill in your own values
docker compose up --build
```

The API starts on **http://localhost:8081**. The admin account is `admin@inventory.com`
with the `ADMIN_PASSWORD` from your `.env`.

| Command | What it does |
|---|---|
| `docker compose down` | Stops the containers and keeps the data |
| `docker compose down -v` | Also deletes the database, for a fresh start |

### Run locally

You need Java 21 and MySQL 8 running locally (the `inventoryapi` database is created on first start).

Secrets are not committed. Create `config/application.properties` in the project root
(it is gitignored, and Spring Boot loads it automatically on top of the default settings):

```properties
# MySQL account
spring.datasource.username=root
spring.datasource.password=<your MySQL password>

# Base64 key of at least 32 bytes, e.g. from: openssl rand -base64 48
app.jwt.secret=<your secret>

# Admin account created on first start if it doesn't exist
app.admin.email=admin@inventory.com
app.admin.password=<choose a password>
```

Then run:

```bash
./mvnw spring-boot:run
```

The API starts on **http://localhost:8081**.

### Profiles

| Profile | When | Settings |
|---|---|---|
| `dev` | Default when no profile is set (running locally) | Local MySQL, SQL logging, Flyway baseline for a pre-existing database |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` (set by Docker Compose) | Database from `DB_URL` / `DB_USERNAME`, no SQL logging, no defaults: a missing variable stops the app at startup |

## API documentation

Open **http://localhost:8081/swagger-ui.html** to browse and try every endpoint.

1. Sign up with `POST /api/users`, or use the admin account from your configuration
2. Call `POST /api/auth/login` and copy the `accessToken` from the response
3. Click **Authorize**, paste the token, and every request will send it

The raw OpenAPI document is at `/v3/api-docs`.

## Authentication

| Endpoint | What it does |
|---|---|
| `POST /api/auth/login` | Returns an access token (15 min) and a refresh token (7 days) |
| `POST /api/auth/refresh` | Exchanges a refresh token for a new pair; the old refresh token stops working |
| `POST /api/auth/logout` | Revokes the refresh token |

Send the access token as `Authorization: Bearer <token>`.

**How the refresh tokens are protected:**

- They are random strings, not JWTs, and the database stores only their SHA-256 hash,
  so a leaked database can't be used to log in
- Each one works **once**. Reusing an old refresh token is treated as theft
  and revokes every session of that user
- The user is reloaded on every request, so a deactivated account loses access immediately,
  even with a valid access token

## Roles and permissions

| Action | STAFF | ADMIN |
|---|:---:|:---:|
| Read products, categories, locations, stock | ✅ | ✅ |
| Create products, record stock movements and transfers | ✅ | ✅ |
| Edit or deactivate a product | own products | ✅ |
| Manage categories and locations | | ✅ |
| List users, change roles, reactivate users | | ✅ |
| Edit own profile, deactivate own account | ✅ | ✅ |

Sign-up is **deliberately open** so anyone can try the API; new accounts are `STAFF`.
The last active admin can't be demoted or deactivated.

## Design notes

- **Stock and its history never disagree.** A movement and the stock level it changes are
  saved in the same transaction, and the stock level row is locked while it changes,
  so concurrent requests can't oversell
- **No deadlocks between opposite transfers.** A transfer always locks the lower location id first,
  so A→B and B→A at the same time can't wait on each other
- **Soft deletes.** Products, categories, locations and users are deactivated, not deleted,
  because stock movements keep pointing at them. A product or location that still holds stock
  can't be deactivated
- **Errors.** Validation, unknown sort fields, bad enum values, missing resources and conflicts
  all return a `ProblemDetail` with the right status (400, 401, 403, 404, 409) instead of a stack trace
- **Versioned schema.** Flyway owns the database schema through SQL scripts in `src/main/resources/db/migration`.
  Hibernate only validates that the entities match the tables, so a mismatch stops the app at startup

## Tests

```bash
./mvnw test
```

Docker must be running: integration tests start a throwaway MySQL 8.4 container with Testcontainers,
so they run against the same database engine and Flyway migrations as production, never against your local data.

| Test | What it proves |
|---|---|
| `StockLevelTest` | Unit tests of the stock rules: increases, decreases, and refusing to go below zero |
| `StockConcurrencyTest` | 20 threads buy the last 10 items at once: exactly 10 succeed and stock ends at zero, thanks to the row lock |
| `StockTransferTest` | A transfer moves stock between locations; an oversized one changes nothing |
| `AuthFlowTest` | Login, refresh token rotation, and reuse detection revoking every token of the user |
| `UserAccessTest` | 401 without a token, 403 for the wrong role, no self-assigned admin role, and the last admin can't be removed or demoted |

The concurrency and reuse-detection tests were checked by removing the code they guard
(the `@Lock`, the `noRollbackFor`) and confirming they fail.

## Project structure

```
src/main/java/com/inventoryAPIs/demo
├── config        Security, OpenAPI and admin seeding
├── controller    REST endpoints
├── dto           Request and response bodies
├── entity        JPA entities
├── exception     Custom exceptions and the global error handler
├── repository    Spring Data repositories and query specifications
├── security      JWT filter, token service and ownership checks
└── service       Business logic and transactions
```

## Roadmap

- Dev and prod profiles, health checks and metrics, structured logging, API versioning
- Load testing with k6, then caching and multiple instances behind nginx
- Rate limiting with Bucket4j and Redis
- Orders with expiring stock reservations, and Stripe payments (test mode)
- CI with GitHub Actions and a cloud deployment
