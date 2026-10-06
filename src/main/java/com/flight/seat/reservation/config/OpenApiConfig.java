package com.flight.seat.reservation.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Flight Seat Reservation Service",
                version = "v1",
                description = """
                        API for creating flights, reserving and confirming seats, and managing passengers.

                        Note: flight departure times are absolute instants — send `departureDateTime`
                        in UTC (ISO-8601 with a trailing `Z`, e.g. 2026-10-06T12:00:00Z).
                        Seats can only be booked up to 45 minutes before departure.
                        """
        )
)
public class OpenApiConfig {
}
