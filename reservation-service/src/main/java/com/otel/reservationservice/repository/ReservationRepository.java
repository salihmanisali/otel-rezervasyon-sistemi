package com.otel.reservationservice.repository;

import com.otel.reservationservice.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByHotelId(Long hotelId);

    List<Reservation> findByRoomId(Long roomId);

    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId " +
            "AND ((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate) " +
            "OR (r.checkInDate >= :checkInDate AND r.checkInDate < :checkOutDate))")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}