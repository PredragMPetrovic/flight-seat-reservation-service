package com.flight.seat.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.seat.reservation.seat.scheduler.SeatScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "booking.hold-minutes=15",
                "booking.sweep-interval-ms=3600000"
        })
class ReservationIntegrationTest {

    /** A clock whose instant can be moved forward so hold expiry is deterministic. */
    static class MutableClock extends Clock {
        private volatile Instant instant;

        MutableClock(Instant start) {
            this.instant = start;
        }

        void advance(Duration d) {
            this.instant = this.instant.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @TestConfiguration
    static class ClockOverride {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(Instant.parse("2027-01-01T00:00:00Z"));
        }
    }

    @LocalServerPort
    private int port;
    @Autowired
    private SeatScheduler seatScheduler;
    @Autowired
    private MutableClock clock;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String FUTURE_DEPARTURE = "2027-06-01T12:00:00Z";

    private HttpResponse<String> send(String method, String path, String body) {
        try {
            HttpRequest.BodyPublisher pub = body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .header("Content-Type", "application/json")
                    .method(method, pub)
                    .build();
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode body(HttpResponse<String> r) {
        try {
            return mapper.readTree(r.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long createPassenger(String name) {
        HttpResponse<String> r = send("POST", "/api/v1/passengers", "{\"name\":\"" + name + "\"}");
        assertThat(r.statusCode()).isBetween(200, 299);
        return body(r).get("id").asLong();
    }

    private long createFlight() {
        String b = "{\"departureCity\":\"Dublin\",\"departureAirport\":\"DUB\","
                + "\"destinationCity\":\"London\",\"destinationAirport\":\"LHR\","
                + "\"departureDateTime\":\"" + FUTURE_DEPARTURE + "\"}";
        HttpResponse<String> r = send("POST", "/api/v1/admin/flights", b);
        assertThat(r.statusCode()).isBetween(200, 299);
        return body(r).get("id").asLong();
    }

    private HttpResponse<String> reserve(long flightId, String seat, long passengerId) {
        String b = "{\"seatNumber\":\"" + seat + "\",\"passengerId\":\"" + passengerId + "\"}";
        return send("POST", "/api/v1/flights/" + flightId + "/bookings", b);
    }

    @Test
    void samePassengerCanHoldMultipleSeats() {
        long flight = createFlight();
        long passenger = createPassenger("multi");

        assertThat(reserve(flight, "1A", passenger).statusCode()).isBetween(200, 299);
        assertThat(reserve(flight, "1B", passenger).statusCode()).isBetween(200, 299);
    }

    @Test
    void deletingFlightWithBookingsSucceeds() {
        long flight = createFlight();
        long passenger = createPassenger("delflight");
        reserve(flight, "1A", passenger);

        HttpResponse<String> r = send("DELETE", "/api/v1/admin/flights/" + flight, null);

        assertThat(r.statusCode()).isEqualTo(204);
    }

    @Test
    void validHoldCanBeConfirmed() {
        long flight = createFlight();
        long passenger = createPassenger("confirm-ok");
        long bookingId = body(reserve(flight, "1A", passenger)).get("id").asLong();

        HttpResponse<String> r = send("POST", "/api/v1/bookings/" + bookingId + "/confirm", null);

        assertThat(r.statusCode()).isBetween(200, 299);
        assertThat(body(r).get("bookingStatus").asText()).isEqualTo("CONFIRMED");
    }

    @Test
    void expiredHoldCannotBeConfirmed() {
        long flight = createFlight();
        long passenger = createPassenger("confirm-expired");
        long bookingId = body(reserve(flight, "1A", passenger)).get("id").asLong();

        clock.advance(Duration.ofMinutes(20));

        HttpResponse<String> r = send("POST", "/api/v1/bookings/" + bookingId + "/confirm", null);

        assertThat(r.statusCode()).isEqualTo(410);
    }

    @Test
    void expiredHoldIsSweptAndSeatBecomesRebookable() {
        long flight = createFlight();
        long p1 = createPassenger("sweep-1");
        long p2 = createPassenger("sweep-2");
        long bookingId = body(reserve(flight, "1C", p1)).get("id").asLong();

        clock.advance(Duration.ofMinutes(20));
        seatScheduler.releaseExpiredHolds();

        JsonNode bookings = body(send("GET", "/api/v1/bookings", null));
        boolean stillThere = false;
        for (JsonNode b : bookings) {
            if (b.get("id").asLong() == bookingId) {
                stillThere = true;
            }
        }
        assertThat(stillThere).isFalse();

        assertThat(reserve(flight, "1C", p2).statusCode()).isBetween(200, 299);
    }

    @Test
    void concurrentReservationsOfSameSeatYieldExactlyOneSuccess() throws Exception {
        long flight = createFlight();
        int threads = 20;
        long[] passengers = new long[threads];
        for (int i = 0; i < threads; i++) {
            passengers[i] = createPassenger("race-" + i);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                long passenger = passengers[i];
                futures.add(pool.submit(() -> {
                    int status = reserve(flight, "3A", passenger).statusCode();
                    if (status >= 200 && status < 300) {
                        success.incrementAndGet();
                    } else if (status == 409) {
                        conflict.incrementAndGet();
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(success.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(threads - 1);
    }

    @Test
    void createFlightWithoutDepartureTimeReturns400() {
        String b = "{\"departureCity\":\"Dublin\",\"departureAirport\":\"DUB\","
                + "\"destinationCity\":\"London\",\"destinationAirport\":\"LHR\"}";
        assertThat(send("POST", "/api/v1/admin/flights", b).statusCode()).isEqualTo(400);
    }

    @Test
    void malformedJsonReturns400() {
        assertThat(send("POST", "/api/v1/passengers", "{\"name\": \"broken").statusCode()).isEqualTo(400);
    }

    @Test
    void invalidDateFilterReturns400() {
        assertThat(send("GET", "/api/v1/flights?date=not-a-date", null).statusCode()).isEqualTo(400);
    }

    @Test
    void unsupportedMethodReturns405() {
        assertThat(send("PATCH", "/api/v1/passengers", null).statusCode()).isEqualTo(405);
    }
}
