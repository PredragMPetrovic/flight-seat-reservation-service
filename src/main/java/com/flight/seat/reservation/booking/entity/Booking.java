package com.flight.seat.reservation.booking.entity;

import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.passenger.entity.Passenger;
import com.flight.seat.reservation.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity
@Table(name = "booking")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @OneToOne
    @JoinColumn(name = "seat_id", unique = true)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Instant holdExpiresAt;
}
