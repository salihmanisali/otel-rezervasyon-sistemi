package com.otel.reservationservice.service;

import com.otel.common.event.ReservationCreatedEvent;
import com.otel.reservationservice.domain.Reservation;
import com.otel.reservationservice.dto.ReservationDTO;
import com.otel.reservationservice.exception.ReservationConflictException;
import com.otel.reservationservice.exception.ResourceNotFoundException;
import com.otel.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private final String TOPIC = "reservation-created";
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks
    private ReservationService reservationService;
    @Captor
    private ArgumentCaptor<ReservationCreatedEvent> eventCaptor;
    private Reservation reservation1;
    private Reservation reservation2;
    private ReservationDTO reservationDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        reservation1 = Reservation.builder()
                .id(1L)
                .hotelId(1L)
                .roomId(101L)
                .guestName("John Doe")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .status("CREATED")
                .build();

        reservation2 = Reservation.builder()
                .id(2L)
                .hotelId(1L)
                .roomId(102L)
                .guestName("Jane Smith")
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .createdAt(LocalDateTime.now())
                .status("CREATED")
                .build();

        reservationDTO = ReservationDTO.builder()
                .id(1L)
                .hotelId(1L)
                .roomId(101L)
                .guestName("John Doe")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .status("CREATED")
                .build();

        // Set the Kafka topic name using reflection
        ReflectionTestUtils.setField(reservationService, "reservationCreatedTopic", TOPIC);
    }

    @Test
    void getAllReservations_ShouldReturnAllReservations() {
        // Given
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation1, reservation2));

        // When
        List<ReservationDTO> result = reservationService.getAllReservations();

        // Then
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getGuestName());
        assertEquals("Jane Smith", result.get(1).getGuestName());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getReservationById_WhenReservationExists_ShouldReturnReservation() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));

        // When
        ReservationDTO result = reservationService.getReservationById(1L);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getGuestName());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void getReservationById_WhenReservationDoesNotExist_ShouldThrowException() {
        // Given
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> reservationService.getReservationById(99L));
        verify(reservationRepository, times(1)).findById(99L);
    }

    @Test
    void getReservationsByHotelId_ShouldReturnReservationsForHotel() {
        // Given
        when(reservationRepository.findByHotelId(1L)).thenReturn(Arrays.asList(reservation1, reservation2));

        // When
        List<ReservationDTO> result = reservationService.getReservationsByHotelId(1L);

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getHotelId());
        assertEquals(1L, result.get(1).getHotelId());
        verify(reservationRepository, times(1)).findByHotelId(1L);
    }

    @Test
    void getReservationsByRoomId_ShouldReturnReservationsForRoom() {
        // Given
        when(reservationRepository.findByRoomId(101L)).thenReturn(Collections.singletonList(reservation1));

        // When
        List<ReservationDTO> result = reservationService.getReservationsByRoomId(101L);

        // Then
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getRoomId());
        verify(reservationRepository, times(1)).findByRoomId(101L);
    }

    @Test
    void createReservation_WhenValidReservation_ShouldSaveAndSendEvent() {
        // Given
        when(reservationRepository.findOverlappingReservations(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation1);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        // When
        ReservationDTO result = reservationService.createReservation(reservationDTO);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getGuestName());
        verify(reservationRepository, times(1)).findOverlappingReservations(
                eq(reservationDTO.getRoomId()),
                eq(reservationDTO.getCheckInDate()),
                eq(reservationDTO.getCheckOutDate())
        );
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), eventCaptor.capture());

        ReservationCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getReservationId());
        assertEquals("John Doe", capturedEvent.getGuestName());
    }

    @Test
    void createReservation_WhenInvalidDates_ShouldThrowException() {
        // Given
        ReservationDTO invalidDTO = ReservationDTO.builder()
                .hotelId(1L)
                .roomId(101L)
                .guestName("John Doe")
                .checkInDate(LocalDate.now().plusDays(3))
                .checkOutDate(LocalDate.now().plusDays(1))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(invalidDTO));
        verify(reservationRepository, never()).findOverlappingReservations(any(), any(), any());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void createReservation_WhenConflictingReservation_ShouldThrowException() {
        // Given
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Collections.singletonList(reservation1));

        // When & Then
        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(reservationDTO));
        verify(reservationRepository, times(1)).findOverlappingReservations(
                eq(reservationDTO.getRoomId()),
                eq(reservationDTO.getCheckInDate()),
                eq(reservationDTO.getCheckOutDate())
        );
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void updateReservation_WhenValidReservation_ShouldUpdateAndReturn() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));
        when(reservationRepository.findOverlappingReservations(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation1);

        ReservationDTO updateDTO = ReservationDTO.builder()
                .id(1L)
                .hotelId(1L)
                .roomId(101L)
                .guestName("John Doe Updated")
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .status("UPDATED")
                .build();

        // When
        ReservationDTO result = reservationService.updateReservation(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("John Doe Updated", result.getGuestName());
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).findOverlappingReservations(
                eq(updateDTO.getRoomId()),
                eq(updateDTO.getCheckInDate()),
                eq(updateDTO.getCheckOutDate())
        );
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void updateReservation_WhenReservationDoesNotExist_ShouldThrowException() {
        // Given
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> reservationService.updateReservation(99L, reservationDTO));
        verify(reservationRepository, times(1)).findById(99L);
        verify(reservationRepository, never()).findOverlappingReservations(any(), any(), any());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void deleteReservation_WhenReservationExists_ShouldDeleteReservation() {
        // Given
        when(reservationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(1L);

        // When
        reservationService.deleteReservation(1L);

        // Then
        verify(reservationRepository, times(1)).existsById(1L);
        verify(reservationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteReservation_WhenReservationDoesNotExist_ShouldThrowException() {
        // Given
        when(reservationRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> reservationService.deleteReservation(99L));
        verify(reservationRepository, times(1)).existsById(99L);
        verify(reservationRepository, never()).deleteById(any());
    }
}
