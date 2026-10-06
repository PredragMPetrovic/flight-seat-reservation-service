package com.flight.seat.reservation.flight.dto;

import com.flight.seat.reservation.seat.dto.SeatDTO;
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
    String departureCity;
    String departureAirport;
    String destinationCity;
    String destinationAirport;
    Instant departureDateTime;
    List<SeatDTO> seats;
}