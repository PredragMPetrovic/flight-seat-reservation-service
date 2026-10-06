package com.flight.seat.reservation.flight.dto;

import com.flight.seat.reservation.seat.dto.SeatDTO;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class FlightDTO {
    Long id;
    @NotBlank
    String departureCity;
    @NotBlank
    String departureAirport;
    @NotBlank
    String destinationCity;
    @NotBlank
    String destinationAirport;
    @NotNull
    @Future
    Instant departureDateTime;
    List<SeatDTO> seats;
}