package com.flight.seat.reservation.booking.dto;

import com.flight.seat.reservation.booking.enums.BookingStatus;
import lombok.*;

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
}
