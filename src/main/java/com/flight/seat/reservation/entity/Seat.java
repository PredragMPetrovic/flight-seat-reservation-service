package com.flight.seat.reservation.entity;

import com.flight.seat.reservation.dto.SeatDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "seat")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @ToString.Exclude
    private Flight flight;

    public SeatDTO toDTO() {
        return SeatDTO.builder()
                .seatNumber(this.seatNumber)
                .build();
    }
}
