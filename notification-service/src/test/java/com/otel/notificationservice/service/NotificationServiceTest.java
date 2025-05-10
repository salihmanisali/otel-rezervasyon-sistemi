package com.otel.notificationservice.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.otel.common.event.ReservationCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private NotificationService notificationService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();

        // Get the logger for the NotificationService class
        logger = (Logger) LoggerFactory.getLogger(NotificationService.class);

        // Create and start a ListAppender to capture log messages
        listAppender = new ListAppender<>();
        listAppender.start();

        // Add the appender to the logger
        logger.addAppender(listAppender);
    }

    @Test
    void handleReservationCreatedEvent_ShouldLogEventDetails() {
        // Given
        ReservationCreatedEvent event = ReservationCreatedEvent.builder()
                .reservationId(1L)
                .hotelId(10L)
                .roomId(100L)
                .guestName("John Doe")
                .checkInDate(LocalDate.of(2025, 2, 10))
                .checkOutDate(LocalDate.of(2025, 2, 15))
                .build();

        // When
        notificationService.handleReservationCreatedEvent(event);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;

        // Verify that 3 log messages were created (as per the implementation)
        assertEquals(3, logsList.size());

        // Verify the log levels
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals(Level.INFO, logsList.get(1).getLevel());
        assertEquals(Level.INFO, logsList.get(2).getLevel());

        // Verify the log messages
        assertTrue(logsList.get(0).getFormattedMessage().contains("Received reservation created event"));
        assertTrue(logsList.get(1).getFormattedMessage().contains("Sending notification for reservation: 1 to guest: John Doe"));
        assertTrue(logsList.get(2).getFormattedMessage().contains("Reservation details: Hotel ID: 10, Room ID: 100, Check-in: 2025-02-10, Check-out: 2025-02-15"));
    }
}