package com.flight.seat.reservation.seat.dto;

import com.flight.seat.reservation.seat.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SeatDTO {
    private String seatNumber;
    private SeatStatus status;
    private String passengerId;
}
