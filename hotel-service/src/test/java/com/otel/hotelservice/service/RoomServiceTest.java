package com.otel.hotelservice.service;

import com.otel.hotelservice.domain.Room;
import com.otel.hotelservice.dto.RoomDTO;
import com.otel.hotelservice.exception.ResourceNotFoundException;
import com.otel.hotelservice.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room1;
    private Room room2;
    private RoomDTO roomDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        room1 = Room.builder()
                .id(1L)
                .hotelId(1L)
                .roomNumber("101")
                .capacity(2)
                .pricePerNight(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        room2 = Room.builder()
                .id(2L)
                .hotelId(1L)
                .roomNumber("102")
                .capacity(4)
                .pricePerNight(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        roomDTO = new RoomDTO();
        roomDTO.setId(1L);
        roomDTO.setHotelId(1L);
        roomDTO.setRoomNumber("101");
        roomDTO.setCapacity(2);
        roomDTO.setPricePerNight(new BigDecimal("100.00"));
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() {
        // Given
        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2));

        // When
        List<RoomDTO> result = roomService.getAllRooms();

        // Then
        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getRoomNumber());
        assertEquals("102", result.get(1).getRoomNumber());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void getRoomsByHotelId_ShouldReturnRoomsForHotel() {
        // Given
        when(roomRepository.findByHotelId(1L)).thenReturn(Arrays.asList(room1, room2));

        // When
        List<RoomDTO> result = roomService.getRoomsByHotelId(1L);

        // Then
        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getRoomNumber());
        assertEquals("102", result.get(1).getRoomNumber());
        verify(roomRepository, times(1)).findByHotelId(1L);
    }

    @Test
    void getRoomById_WhenRoomExists_ShouldReturnRoom() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));

        // When
        RoomDTO result = roomService.getRoomById(1L);

        // Then
        assertNotNull(result);
        assertEquals("101", result.getRoomNumber());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void getRoomById_WhenRoomDoesNotExist_ShouldThrowException() {
        // Given
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(99L));
        verify(roomRepository, times(1)).findById(99L);
    }

    @Test
    void createRoom_ShouldSaveAndReturnRoom() {
        // Given
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        // When
        RoomDTO result = roomService.createRoom(roomDTO);

        // Then
        assertNotNull(result);
        assertEquals("101", result.getRoomNumber());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void updateRoom_WhenRoomExists_ShouldUpdateAndReturnRoom() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        roomDTO.setRoomNumber("101-Updated");
        roomDTO.setCapacity(3);
        roomDTO.setPricePerNight(new BigDecimal("120.00"));

        // When
        RoomDTO result = roomService.updateRoom(1L, roomDTO);

        // Then
        assertNotNull(result);
        assertEquals("101-Updated", result.getRoomNumber());
        assertEquals(3, result.getCapacity());
        assertEquals(new BigDecimal("120.00"), result.getPricePerNight());
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void updateRoom_WhenRoomDoesNotExist_ShouldThrowException() {
        // Given
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roomService.updateRoom(99L, roomDTO));
        verify(roomRepository, times(1)).findById(99L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void deleteRoom_WhenRoomExists_ShouldDeleteRoom() {
        // Given
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        // When
        roomService.deleteRoom(1L);

        // Then
        verify(roomRepository, times(1)).existsById(1L);
        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRoom_WhenRoomDoesNotExist_ShouldThrowException() {
        // Given
        when(roomRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(99L));
        verify(roomRepository, times(1)).existsById(99L);
        verify(roomRepository, never()).deleteById(any());
    }
}