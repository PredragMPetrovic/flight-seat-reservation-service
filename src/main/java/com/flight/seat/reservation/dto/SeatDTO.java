package com.flight.seat.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatDTO {
    private String seatNumber;
}
