package com.otel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otel.hotelservice.dto.RoomDTO;
import com.otel.hotelservice.exception.ResourceNotFoundException;
import com.otel.hotelservice.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    private RoomDTO roomDTO1;
    private RoomDTO roomDTO2;
    private List<RoomDTO> roomDTOs;

    @BeforeEach
    void setUp() {
        roomDTO1 = new RoomDTO();
        roomDTO1.setId(1L);
        roomDTO1.setHotelId(1L);
        roomDTO1.setRoomNumber("101");
        roomDTO1.setCapacity(2);
        roomDTO1.setPricePerNight(new BigDecimal("100.00"));

        roomDTO2 = new RoomDTO();
        roomDTO2.setId(2L);
        roomDTO2.setHotelId(1L);
        roomDTO2.setRoomNumber("102");
        roomDTO2.setCapacity(4);
        roomDTO2.setPricePerNight(new BigDecimal("150.00"));

        roomDTOs = Arrays.asList(roomDTO1, roomDTO2);
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() throws Exception {
        when(roomService.getAllRooms()).thenReturn(roomDTOs);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].roomNumber", is("101")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].roomNumber", is("102")));

        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    void getRoomsByHotelId_ShouldReturnRoomsForHotel() throws Exception {
        when(roomService.getRoomsByHotelId(1L)).thenReturn(roomDTOs);

        mockMvc.perform(get("/api/rooms/hotel/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].roomNumber", is("101")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].roomNumber", is("102")));

        verify(roomService, times(1)).getRoomsByHotelId(1L);
    }

    @Test
    void getRoom_WhenRoomExists_ShouldReturnRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(roomDTO1);

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", is("101")));

        verify(roomService, times(1)).getRoomById(1L);
    }

    @Test
    void getRoom_WhenRoomDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(roomService.getRoomById(99L)).thenThrow(new ResourceNotFoundException("Room not found with id: 99"));

        mockMvc.perform(get("/api/rooms/99"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).getRoomById(99L);
    }

    @Test
    void createRoom_ShouldCreateAndReturnRoom() throws Exception {
        when(roomService.createRoom(any(RoomDTO.class))).thenReturn(roomDTO1);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO1)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", is("101")));

        verify(roomService, times(1)).createRoom(any(RoomDTO.class));
    }

    @Test
    void updateRoom_WhenRoomExists_ShouldUpdateAndReturnRoom() throws Exception {
        when(roomService.updateRoom(eq(1L), any(RoomDTO.class))).thenReturn(roomDTO1);

        mockMvc.perform(put("/api/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roomNumber", is("101")));

        verify(roomService, times(1)).updateRoom(eq(1L), any(RoomDTO.class));
    }

    @Test
    void updateRoom_WhenRoomDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(roomService.updateRoom(eq(99L), any(RoomDTO.class)))
                .thenThrow(new ResourceNotFoundException("Room not found with id: 99"));

        mockMvc.perform(put("/api/rooms/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO1)))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).updateRoom(eq(99L), any(RoomDTO.class));
    }

    @Test
    void deleteRoom_WhenRoomExists_ShouldDeleteRoom() throws Exception {
        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).deleteRoom(1L);
    }

    @Test
    void deleteRoom_WhenRoomDoesNotExist_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Room not found with id: 99"))
                .when(roomService).deleteRoom(99L);

        mockMvc.perform(delete("/api/rooms/99"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).deleteRoom(99L);
    }
}
