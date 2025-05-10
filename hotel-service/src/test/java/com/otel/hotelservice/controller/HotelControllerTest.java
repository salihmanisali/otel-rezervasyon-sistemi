package com.otel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otel.hotelservice.dto.HotelDTO;
import com.otel.hotelservice.exception.ResourceNotFoundException;
import com.otel.hotelservice.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HotelService hotelService;

    private HotelDTO hotelDTO1;
    private HotelDTO hotelDTO2;
    private List<HotelDTO> hotelDTOs;

    @BeforeEach
    void setUp() {
        hotelDTO1 = new HotelDTO();
        hotelDTO1.setId(1L);
        hotelDTO1.setName("Grand Hotel");
        hotelDTO1.setAddress("123 Main St");
        hotelDTO1.setStarRating(4);

        hotelDTO2 = new HotelDTO();
        hotelDTO2.setId(2L);
        hotelDTO2.setName("Luxury Resort");
        hotelDTO2.setAddress("456 Beach Rd");
        hotelDTO2.setStarRating(5);

        hotelDTOs = Arrays.asList(hotelDTO1, hotelDTO2);
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(hotelDTOs);

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Grand Hotel")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Luxury Resort")));

        verify(hotelService, times(1)).getAllHotels();
    }

    @Test
    void getHotel_WhenHotelExists_ShouldReturnHotel() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(hotelDTO1);

        mockMvc.perform(get("/api/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Grand Hotel")));

        verify(hotelService, times(1)).getHotelById(1L);
    }

    @Test
    void getHotel_WhenHotelDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(hotelService.getHotelById(99L)).thenThrow(new ResourceNotFoundException("Hotel not found with id: 99"));

        mockMvc.perform(get("/api/hotels/99"))
                .andExpect(status().isNotFound());

        verify(hotelService, times(1)).getHotelById(99L);
    }

    @Test
    void createHotel_ShouldCreateAndReturnHotel() throws Exception {
        when(hotelService.createHotel(any(HotelDTO.class))).thenReturn(hotelDTO1);

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelDTO1)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Grand Hotel")));

        verify(hotelService, times(1)).createHotel(any(HotelDTO.class));
    }

    @Test
    void updateHotel_WhenHotelExists_ShouldUpdateAndReturnHotel() throws Exception {
        when(hotelService.updateHotel(eq(1L), any(HotelDTO.class))).thenReturn(hotelDTO1);

        mockMvc.perform(put("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelDTO1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Grand Hotel")));

        verify(hotelService, times(1)).updateHotel(eq(1L), any(HotelDTO.class));
    }

    @Test
    void updateHotel_WhenHotelDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(hotelService.updateHotel(eq(99L), any(HotelDTO.class)))
                .thenThrow(new ResourceNotFoundException("Hotel not found with id: 99"));

        mockMvc.perform(put("/api/hotels/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelDTO1)))
                .andExpect(status().isNotFound());

        verify(hotelService, times(1)).updateHotel(eq(99L), any(HotelDTO.class));
    }

    @Test
    void deleteHotel_WhenHotelExists_ShouldDeleteHotel() throws Exception {
        doNothing().when(hotelService).deleteHotel(1L);

        mockMvc.perform(delete("/api/hotels/1"))
                .andExpect(status().isNoContent());

        verify(hotelService, times(1)).deleteHotel(1L);
    }

    @Test
    void deleteHotel_WhenHotelDoesNotExist_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Hotel not found with id: 99"))
                .when(hotelService).deleteHotel(99L);

        mockMvc.perform(delete("/api/hotels/99"))
                .andExpect(status().isNotFound());

        verify(hotelService, times(1)).deleteHotel(99L);
    }
}