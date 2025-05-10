package com.otel.hotelservice.repository;


import com.otel.hotelservice.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
