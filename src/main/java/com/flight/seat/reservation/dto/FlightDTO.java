package com.flight.seat.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class FlightDTO {
    String departureCity;
    String departureAirport;
    String destinationCity;
    String destinationAirport;
    LocalDateTime departureDateTime;
    List<SeatDTO> seats;
}