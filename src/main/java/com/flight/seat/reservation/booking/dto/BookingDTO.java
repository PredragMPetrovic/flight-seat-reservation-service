package com.flight.seat.reservation.booking.dto;

import com.flight.seat.reservation.booking.enums.BookingStatus;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class BookingDTO {
    Long id;
    Long flightId;
    Long seatId;
    Long passengerId;
    BookingStatus bookingStatus;
    Instant holdExpiresAt;
}
