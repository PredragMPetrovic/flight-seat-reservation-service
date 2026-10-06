package com.flight.seat.reservation.seat.dto;

import com.flight.seat.reservation.seat.enums.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SeatDTO {
    @NotBlank
    private String seatNumber;
    private SeatStatus status;
    @NotBlank
    private String passengerId;
}
