package com.otel.hotelservice.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDTO {
    private Long id;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @Positive(message = "Capacity must be positive")
    private int capacity;

    @Positive(message = "Price per night must be positive")
    private BigDecimal pricePerNight;
}
