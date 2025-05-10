package com.otel.hotelservice.service;

import com.otel.hotelservice.domain.Hotel;
import com.otel.hotelservice.dto.HotelDTO;
import com.otel.hotelservice.exception.ResourceNotFoundException;
import com.otel.hotelservice.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel1;
    private Hotel hotel2;
    private HotelDTO hotelDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        hotel1 = Hotel.builder()
                .id(1L)
                .name("Grand Hotel")
                .address("123 Main St")
                .starRating(4)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotel2 = Hotel.builder()
                .id(2L)
                .name("Luxury Resort")
                .address("456 Beach Rd")
                .starRating(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotelDTO = new HotelDTO();
        hotelDTO.setId(1L);
        hotelDTO.setName("Grand Hotel");
        hotelDTO.setAddress("123 Main St");
        hotelDTO.setStarRating(4);
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        // Given
        when(hotelRepository.findAll()).thenReturn(Arrays.asList(hotel1, hotel2));

        // When
        List<HotelDTO> result = hotelService.getAllHotels();

        // Then
        assertEquals(2, result.size());
        assertEquals("Grand Hotel", result.get(0).getName());
        assertEquals("Luxury Resort", result.get(1).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotel() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel1));

        // When
        HotelDTO result = hotelService.getHotelById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Grand Hotel", result.getName());
        verify(hotelRepository, times(1)).findById(1L);
    }

    @Test
    void getHotelById_WhenHotelDoesNotExist_ShouldThrowException() {
        // Given
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> hotelService.getHotelById(99L));
        verify(hotelRepository, times(1)).findById(99L);
    }

    @Test
    void createHotel_ShouldSaveAndReturnHotel() {
        // Given
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel1);

        // When
        HotelDTO result = hotelService.createHotel(hotelDTO);

        // Then
        assertNotNull(result);
        assertEquals("Grand Hotel", result.getName());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void updateHotel_WhenHotelExists_ShouldUpdateAndReturnHotel() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel1));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel1);

        hotelDTO.setName("Grand Hotel Updated");
        hotelDTO.setAddress("123 Main St Updated");
        hotelDTO.setStarRating(5);

        // When
        HotelDTO result = hotelService.updateHotel(1L, hotelDTO);

        // Then
        assertNotNull(result);
        assertEquals("Grand Hotel Updated", result.getName());
        assertEquals("123 Main St Updated", result.getAddress());
        assertEquals(5, result.getStarRating());
        verify(hotelRepository, times(1)).findById(1L);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void updateHotel_WhenHotelDoesNotExist_ShouldThrowException() {
        // Given
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> hotelService.updateHotel(99L, hotelDTO));
        verify(hotelRepository, times(1)).findById(99L);
        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void deleteHotel_WhenHotelExists_ShouldDeleteHotel() {
        // Given
        when(hotelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(1L);

        // When
        hotelService.deleteHotel(1L);

        // Then
        verify(hotelRepository, times(1)).existsById(1L);
        verify(hotelRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteHotel_WhenHotelDoesNotExist_ShouldThrowException() {
        // Given
        when(hotelRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> hotelService.deleteHotel(99L));
        verify(hotelRepository, times(1)).existsById(99L);
        verify(hotelRepository, never()).deleteById(any());
    }
}