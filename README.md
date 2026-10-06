# Flight Seat Reservation Service

A Spring Boot service for browsing flights, holding a seat, and confirming a reservation. Seats are held
temporarily while a booking is `PENDING` and are automatically released if the hold is not confirmed in time.

---

## Tech stack

- **Java 25**, **Spring Boot 4.1.1** (Spring Framework 7)
- Spring Web, Spring Data JPA, Spring Security
- **H2** in-memory database (PostgreSQL compatibility mode)
- **Lombok** + **MapStruct** (annotation processors)
- **springdoc / Swagger UI** for API docs
- JUnit 5, Mockito, AssertJ for tests
- Optional: Docker (multi-stage build)

---

## Prerequisites

- JDK 25 (the bundled `./mvnw` wrapper pulls the right Maven version)
- Or Docker, if you prefer to run it containerised

No external database or services are required — H2 runs in-memory.

---

## How to run

### Option 1 — Maven wrapper (recommended)

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

### Option 2 — Build and run the jar

```bash
./mvnw clean package
java -jar target/*-SNAPSHOT.jar
```

### Option 3 — Docker

```bash
docker build -t flight-seat-reservation-service .
docker run -p 8080:8080 flight-seat-reservation-service
```

> **Running from an IDE?** This project uses **Lombok** and **MapStruct**, which are *annotation
> processors*. You must enable **annotation processing** in your IDE (IntelliJ:
> *Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing*),
> otherwise generated code (entity accessors, MapStruct mappers) will be missing and the project
> **will not compile or run**. Running via `./mvnw` does this for you automatically.

---

## How to test it

- **Run the tests** (unit + a DB-backed integration suite):
  ```bash
  ./mvnw test
  ```
- **Swagger UI** (interactive API browser): http://localhost:8080/swagger-ui.html
- **OpenAPI spec (JSON):** http://localhost:8080/v3/api-docs
- **H2 console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:flightdb`
  - User: `sa` (no password)

A step-by-step manual walkthrough with `curl` examples and error cases lives in [`TESTING.md`](TESTING.md).

### API endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET`    | `/api/v1/flights` | List flights; optional filters `?date=YYYY-MM-DD` (UTC), `?departureCity=`, `?destinationCity=` |
| `POST`   | `/api/v1/flights/{id}/bookings` | Reserve a seat (creates a `PENDING` hold) |
| `POST`   | `/api/v1/bookings/{id}/confirm` | Confirm a reservation |
| `GET`    | `/api/v1/bookings` | List bookings |
| `DELETE` | `/api/v1/bookings/{id}` | Cancel a reservation and release the seat |
| `POST`   | `/api/v1/admin/flights` | Create a flight (auto-generates 36 seats, rows 1–6, A–F) |
| `DELETE` | `/api/v1/admin/flights/{id}` | Delete a flight |
| `POST`   | `/api/v1/passengers` | Create a passenger |
| `GET`    | `/api/v1/passengers` | List passengers |

---

## Key design decisions & trade-offs

### Database: H2 in PostgreSQL mode
I chose an **in-memory H2 database** so the project is trivial to run and test — for both me and the
reviewer — with zero setup (no containers or external services to start). I run it in **PostgreSQL
compatibility mode** because PostgreSQL is the database I have worked with most recently, have set up
locally, and feel comfortable with; running H2 in that dialect keeps the SQL/behaviour close to what a
real Postgres deployment would look like. Schema is created via `ddl-auto=update` for convenience. In a
real deployment this would be a managed PostgreSQL instance with versioned migrations (Flyway/Liquibase).

### Security: permit-all on purpose
Security is deliberately configured to **permit all requests**. Standing up a full identity provider
(e.g. Keycloak) for a take-home would add a lot of setup friction for little value here, so instead I
**outlined where authorization would apply** rather than enforcing it:
- `SecurityConfig` currently has `anyRequest().permitAll()` (CSRF disabled, H2 console allowed).
- `@PreAuthorize` annotations are present on the admin/booking operations to **document the intended
  role model** (e.g. admin-only flight management), but method security is not enabled, so they are not
  enforced. In a real system I would enable method security and wire it to an OAuth2/OIDC resource
  server (JWT), with roles coming from the identity provider.

### Time handling: `Instant` / UTC everywhere
All moments in time (flight departure, seat-hold expiry) are stored and compared as **`Instant` (UTC)**.
This makes the 45-minute booking-window check and the hold-expiry check unambiguous regardless of the
caller's or server's timezone — an instant is an absolute point in time. Practical consequences:
- Clients send and receive timestamps in **UTC / ISO-8601** (e.g. `2026-10-06T12:00:00Z`).
- The `date` filter on `GET /flights` matches the **UTC calendar day**.
- Time is read through an injected `Clock` bean, which keeps the window/expiry logic deterministic and
  unit-testable (`Clock.fixed(...)`).

Trade-off: the assignment frames departures in *local airport time*. UTC is correct and simpler, but it
pushes the "local airport time ↔ instant" conversion to the input layer. The alternative — storing a
local time plus the airport's `ZoneId` — would model the brief more literally at the cost of extra
complexity; I opted for the simpler, unambiguous UTC model.

### Seat holds & automatic release
A reservation creates a `PENDING` booking and marks the seat `RESERVED`, with a per-hold
`holdExpiresAt = now + booking.hold-minutes` (default 15). A scheduled job releases **only holds whose
`holdExpiresAt` has passed** (confirmed bookings are never touched).

I'm aware this is **not a precise 15-minute expiry**: the job sweeps on a fixed interval, so there is a
small delta between when a hold technically expires and when the sweep actually frees the seat. In production I would not rely on a polling job for
this — I'd prefer a mechanism with real expiry semantics, e.g. a **TTL in the datastore** or **Redis
key expiration** (with a listener/keyspace notification), so a hold is released at (or very near) its
exact expiry without a periodic scan.

### Concurrency / no overselling
Preventing double-booking is the core correctness concern, handled with several layers:
- **Pessimistic write lock** when loading the seat to reserve it.
- A **unique constraint on `seat_id`** in the booking table as the ultimate guard against overselling;
  a concurrent conflict surfaces as `DataIntegrityViolationException` → HTTP `409`.
- **Optimistic locking** (`@Version`) on `Seat` and `Booking`, with graceful handling in the confirm
  path and the release scheduler.

### Structure & error handling
- **Package-by-feature** (`flight`, `booking`, `seat`, `passenger`, `admin`) to keep each slice cohesive.
- **DTOs + MapStruct** to decouple the API contract from entities.
- **Bean Validation** (`@Valid` + constraints like `@NotBlank`/`@NotNull`/`@Future`) rejects bad
  input early with `400`s instead of letting it reach the persistence layer.
- Centralised exception handling via a `@RestControllerAdvice` that extends
  `ResponseEntityExceptionHandler`: domain exceptions map to proper status codes (`404`, `409`,
  `410`, …) and framework-level errors (malformed JSON, validation failures, wrong HTTP method)
  map to `400`/`405` rather than surfacing as `500`.

---

## Known limitations / what I'd change for production

- Enforce real authentication/authorization (OIDC + method security) instead of permit-all.
- Replace `ddl-auto=update` with versioned DB migrations on a managed PostgreSQL instance.
- Replace the polling release job with TTL/Redis-based expiry for precise seat-hold release.
- The suite already includes DB-backed integration tests (full HTTP, incl. a concurrent
  reserve-same-seat race) against in-memory H2; for production I'd also run them against real
  PostgreSQL via Testcontainers.
