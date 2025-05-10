package com.otel.notificationservice.service;

import com.otel.common.event.ReservationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "${spring.kafka.topic.reservation-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleReservationCreatedEvent(ReservationCreatedEvent event) {
        log.info("Received reservation created event: {}", event);

        // In a real application, this would send an email or SMS notification
        // For this example, we'll just log the event
        log.info("Sending notification for reservation: {} to guest: {}",
                event.getReservationId(), event.getGuestName());
        log.info("Reservation details: Hotel ID: {}, Room ID: {}, Check-in: {}, Check-out: {}",
                event.getHotelId(), event.getRoomId(), event.getCheckInDate(), event.getCheckOutDate());
    }
}
