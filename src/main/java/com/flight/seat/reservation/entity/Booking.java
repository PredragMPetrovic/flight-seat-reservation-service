package com.flight.seat.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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

    @OneToOne
    @JoinColumn(name = "seat_id", unique = true)
    private Seat seat;

    @OneToOne
    @JoinColumn(name = "passenger_id", unique = true)
    private Passenger passenger;
}
