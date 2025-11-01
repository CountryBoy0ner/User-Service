package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerWebMvcTest {

    private static final String BASE = "/api/cards";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean
    CardService cardService;

    private CardDto dto(long id, String number, String holder) {
        CardDto d = new CardDto();
        d.setId(id);
        d.setNumber(number);
        d.setHolder(holder);
        d.setExpirationDate(LocalDate.of(2035, 5, 31));
        d.setUserId(10L);
        return d;
    }

    @Test
    @DisplayName("GET /api/cards/by-number/{number} -> 200 с DTO")
    void getByNumber_ok() throws Exception {
        when(cardService.getByNumber("4111111111111111"))
                .thenReturn(dto(1L, "4111111111111111", "John Doe"));

        mvc.perform(get(BASE + "/by-number/{number}", "4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("4111111111111111"))
                .andExpect(jsonPath("$.holder").value("John Doe"));

        verify(cardService).getByNumber("4111111111111111");
    }

    @Test
    @DisplayName("GET /api/cards -> 200 со страницей DTO")
    void getAll_ok() throws Exception {
        Pageable req = PageRequest.of(0, 2, Sort.by("id"));
        List<CardDto> content = List.of(
                dto(1L, "4000000000000001", "A"),
                dto(2L, "4000000000000002", "B")
        );
        Page<CardDto> page = new PageImpl<>(content, req, 2);

        when(cardService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get(BASE).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].number").value("4000000000000001"));
    }

    @Test
    @DisplayName("GET /api/cards/by-holder?holder=alice -> 200 с совпадениями")
    void findByHolder_ok() throws Exception {
        Pageable req = PageRequest.of(0, 10);
        Page<CardDto> page = new PageImpl<>(List.of(dto(1L, "4999", "Alice Smith")), req, 1);

        when(cardService.findByHolder(eq("alice"), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get(BASE + "/by-holder")
                        .param("holder", "alice")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].holder").value(containsString("Alice")));
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        doNothing().when(cardService).delete(1L);

        mvc.perform(delete(BASE + "/{id}", 1))
                .andExpect(status().isNoContent());

        verify(cardService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/cards/by-number/{number} -> 204 No Content")
    void deleteByNumber_ok() throws Exception {
        doNothing().when(cardService).deleteByNumber("4111111111111111");

        mvc.perform(delete(BASE + "/by-number/{number}", "4111111111111111"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteByNumber("4111111111111111");
    }
}
