# How to Run & Test the Flight Seat Reservation Service

## Prerequisites

- JDK 25 (only if running with Maven)
- Or just **Docker** (no JDK needed)

The app uses an **in-memory H2 database**, so there's nothing to install or configure — data resets on every restart.

---

## Running the application

### Option 1 — Maven (uses the bundled wrapper)

```bash
./mvnw spring-boot:run
```

### Option 2 — Docker

```bash
docker compose up --build
# or:
docker build -t flight-seat-reservation-service .
docker run --rm -p 8080:8080 flight-seat-reservation-service
```

The service starts on **http://localhost:8080**.

- **Swagger UI (interactive API docs): http://localhost:8080/swagger-ui.html**
OpenAPI spec: `/v3/api-docs`.
- H2 console: **http://localhost:8080/h2-console**
  JDBC URL `jdbc:h2:mem:flightdb`, user `sa`, empty password.
- Security is intentionally open (all endpoints permitted, no login required) so the API is easy to exercise. Role requirements (`ADMIN`, `PASSENGER`) are expressed with `@PreAuthorize` annotations to show *where* each role would be enforced.

---

## ⏰ Important: send departure times in UTC

Flight departure times are stored as an absolute instant, so the `departureDateTime`
field must be sent as **ISO-8601 UTC** — note the trailing **`Z`**:

```
2026-10-06T12:00:00Z
```

The `Z` means UTC. If your local time is, say, UTC+2, then `12:00:00Z` is `14:00` your
local time. Convert accordingly when picking a value.

**Booking window rule:** a seat can only be reserved/confirmed **up to 45 minutes before
departure** (configurable via `booking.cutoff-minutes`). So for the happy path, pick a
departure comfortably in the future.

---

## End-to-end test flow

### 1. Create a passenger

```bash
curl -X POST http://localhost:8080/api/v1/passengers \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Ada Lovelace",
        "email": "ada@example.com",
        "phoneNumber": "+41791234567",
        "address": "Bahnhofstrasse 1, Zurich",
        "passportNumber": "CH123456"
      }'
```

The response contains the generated passenger `id` — use it when reserving a seat.

### 2. Create a flight (ADMIN)

Seats `1A`–`6F` (36 seats) are generated automatically.

```bash
curl -X POST http://localhost:8080/api/v1/admin/flights \
  -H "Content-Type: application/json" \
  -d '{
        "departureCity": "Zurich",
        "departureAirport": "ZRH",
        "destinationCity": "Geneva",
        "destinationAirport": "GVA",
        "departureDateTime": "2026-10-06T12:00:00Z"
      }'
```

### 3. List flights

```bash
curl http://localhost:8080/api/v1/flights
```

Optionally filter by departure date (UTC, `yyyy-MM-dd`) and/or route. Filters can be
combined:

```bash
# by date
curl "http://localhost:8080/api/v1/flights?date=2026-10-06"
# by route
curl "http://localhost:8080/api/v1/flights?departureCity=Zurich&destinationCity=Geneva"
# combined
curl "http://localhost:8080/api/v1/flights?date=2026-10-06&departureCity=Zurich"
```

### 4. Reserve a seat (creates a PENDING booking)

Use the flight id in the path and the passenger id in the body.

```bash
curl -X POST http://localhost:8080/api/v1/flights/1/bookings \
  -H "Content-Type: application/json" \
  -d '{ "seatNumber": "1A", "passengerId": "1" }'
```

### 5. Confirm the reservation (PASSENGER)

```bash
curl -X POST http://localhost:8080/api/v1/bookings/1/confirm
```

The booking becomes `CONFIRMED` and the seat becomes `OCCUPIED`.

### 6. List / delete bookings

```bash
curl http://localhost:8080/api/v1/bookings
curl -X DELETE http://localhost:8080/api/v1/bookings/1   # releases the seat
```

### 7. Delete a flight (ADMIN)

```bash
curl -X DELETE http://localhost:8080/api/v1/admin/flights/1
```

---

## Testing the holds & expiry behavior

A reservation (step 4) is only a **temporary hold**. The booking response includes a
`holdExpiresAt` instant, `booking.hold-minutes` after it was created (default **15 min**).
You can confirm a hold only before it expires — for quick testing, override it to a small
value (see below).

**Happy path:** reserve → confirm within the window → `200` CONFIRMED.

**Expired hold:**

1. Reserve a seat (step 4).
2. Wait longer than `booking.hold-minutes` (15 min by default).
3. Try to confirm:

   ```bash
   curl -i -X POST http://localhost:8080/api/v1/bookings/1/confirm
   ```

   → **`410 Gone`** — the hold has expired and can no longer be confirmed.

4. A background sweep (every `booking.sweep-interval-ms`, 60 s by default) releases the
   expired hold, after which the seat is `AVAILABLE` and can be reserved again by anyone.

Want it faster/slower? Override the properties at startup, e.g.:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--booking.hold-minutes=1 --booking.sweep-interval-ms=5000"
```

---

## Error & validation scenarios (what "good" failures look like)

These confirm the API rejects bad input cleanly instead of returning `500`:

```bash
# Missing/blank required fields → 400 Bad Request
curl -i -X POST http://localhost:8080/api/v1/admin/flights \
  -H "Content-Type: application/json" \
  -d '{ "departureCity": "Zurich" }'

# Departure time in the past → 400 Bad Request
curl -i -X POST http://localhost:8080/api/v1/admin/flights \
  -H "Content-Type: application/json" \
  -d '{ "departureCity":"Zurich","departureAirport":"ZRH","destinationCity":"Geneva","destinationAirport":"GVA","departureDateTime":"2000-01-01T12:00:00Z" }'

# Malformed JSON → 400 Bad Request
curl -i -X POST http://localhost:8080/api/v1/passengers \
  -H "Content-Type: application/json" -d '{ not json'

# Invalid date filter → 400 Bad Request
curl -i "http://localhost:8080/api/v1/flights?date=not-a-date"

# Unsupported method on an existing path → 405 Method Not Allowed
curl -i -X PATCH http://localhost:8080/api/v1/flights/1/bookings

# Reserving a seat that is already taken → 409 Conflict
#   (reserve 1A twice for different passengers)
curl -i -X POST http://localhost:8080/api/v1/flights/1/bookings \
  -H "Content-Type: application/json" -d '{ "seatNumber":"1A","passengerId":"2" }'
```

---
