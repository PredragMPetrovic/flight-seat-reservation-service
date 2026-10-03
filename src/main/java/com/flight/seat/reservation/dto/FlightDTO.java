package com.flight.seat.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class FlightDTO {
    String departureCity;
    String departureAirport;
    String destinationCity;
    String destinationAirport;
    LocalDateTime departureDateTime;
}