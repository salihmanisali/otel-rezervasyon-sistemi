package com.otel.reservationservice.service;

import com.otel.common.event.ReservationCreatedEvent;
import com.otel.reservationservice.domain.Reservation;
import com.otel.reservationservice.dto.ReservationDTO;
import com.otel.reservationservice.exception.ReservationConflictException;
import com.otel.reservationservice.exception.ResourceNotFoundException;
import com.otel.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.reservation-created}")
    private String reservationCreatedTopic;

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        return convertToDTO(reservation);
    }

    public List<ReservationDTO> getReservationsByHotelId(Long hotelId) {
        return reservationRepository.findByHotelId(hotelId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        validateReservationDates(reservationDTO);
        checkForConflictingReservations(reservationDTO);

        Reservation reservation = convertToEntity(reservationDTO);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Send Kafka event
        sendReservationCreatedEvent(savedReservation);

        return convertToDTO(savedReservation);
    }

    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) {
        Reservation existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        validateReservationDates(reservationDTO);

        // Check for conflicts only if dates have changed
        if (!existingReservation.getCheckInDate().equals(reservationDTO.getCheckInDate()) ||
                !existingReservation.getCheckOutDate().equals(reservationDTO.getCheckOutDate()) ||
                !existingReservation.getRoomId().equals(reservationDTO.getRoomId())) {
            checkForConflictingReservations(reservationDTO);
        }

        existingReservation.setHotelId(reservationDTO.getHotelId());
        existingReservation.setRoomId(reservationDTO.getRoomId());
        existingReservation.setGuestName(reservationDTO.getGuestName());
        existingReservation.setCheckInDate(reservationDTO.getCheckInDate());
        existingReservation.setCheckOutDate(reservationDTO.getCheckOutDate());

        Reservation updatedReservation = reservationRepository.save(existingReservation);
        return convertToDTO(updatedReservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
    }

    private void validateReservationDates(ReservationDTO reservationDTO) {
        LocalDate checkInDate = reservationDTO.getCheckInDate();
        LocalDate checkOutDate = reservationDTO.getCheckOutDate();

        if (checkInDate.isEqual(checkOutDate) || checkInDate.isAfter(checkOutDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }

    private void checkForConflictingReservations(ReservationDTO reservationDTO) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                reservationDTO.getRoomId(),
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate()
        );

        if (!overlappingReservations.isEmpty()) {
            throw new ReservationConflictException("Room is already booked for the selected dates");
        }
    }

    private void sendReservationCreatedEvent(Reservation reservation) {
        ReservationCreatedEvent event = ReservationCreatedEvent.builder()
                .reservationId(reservation.getId())
                .hotelId(reservation.getHotelId())
                .roomId(reservation.getRoomId())
                .guestName(reservation.getGuestName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .build();

        log.info("Sending reservation created event: {}", event);
        kafkaTemplate.send(reservationCreatedTopic, event);
    }

    private ReservationDTO convertToDTO(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .hotelId(reservation.getHotelId())
                .roomId(reservation.getRoomId())
                .guestName(reservation.getGuestName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .status(reservation.getStatus())
                .build();
    }

    private Reservation convertToEntity(ReservationDTO dto) {
        return Reservation.builder()
                .hotelId(dto.getHotelId())
                .roomId(dto.getRoomId())
                .guestName(dto.getGuestName())
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .status(dto.getStatus())
                .build();
    }
}
