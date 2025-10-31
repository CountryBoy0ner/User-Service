package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerWebMvcTest {

    private static final String BASE = "/api/users";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean UserService userService;
    @MockitoBean UserMapper userMapper;
    @MockitoBean CardService cardService;

    // --- helpers

    private User mkUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setName("John");
        u.setSurname("Doe");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setEmail(email);
        return u;
    }

    private UserDto mkUserDto(Long id, String email) {
        UserDto d = new UserDto();
        d.setId(id);
        d.setName("John");
        d.setSurname("Doe");
        d.setBirthDate(LocalDate.of(1990, 1, 1));
        d.setEmail(email);
        return d;
    }

    private CardDto mkCardDto(Long id, String number, String holder) {
        CardDto d = new CardDto();
        d.setId(id);
        d.setNumber(number);
        d.setHolder(holder);
        d.setExpirationDate(LocalDate.of(2035, 5, 31));
        d.setUserId(1L);
        return d;
    }

    // --- tests

    @Test
    @DisplayName("POST /api/users -> 201 Created with body + Location")
    void create_ok() throws Exception {
        UserDto request = mkUserDto(null, "john@site.com");
        UserDto created = mkUserDto(1L, "john@site.com");

        when(userService.create(any(UserDto.class))).thenReturn(created);

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE + "/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@site.com"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id} -> 200 OK with updated DTO")
    void patch_ok() throws Exception {
        UserDto patch = new UserDto();
        patch.setName("NewName");
        UserDto updated = mkUserDto(1L, "john@site.com");
        updated.setName("NewName");

        when(userService.patch(eq(1L), any(UserDto.class))).thenReturn(updated);

        mvc.perform(patch(BASE + "/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        doNothing().when(userService).delete(1L);

        mvc.perform(delete(BASE + "/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/by-email?email=... -> 204 No Content")
    void deleteByEmail_ok() throws Exception {
        doNothing().when(userService).deleteByEmail("john@site.com");

        mvc.perform(delete(BASE + "/by-email").param("email", "john@site.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/users/{id} -> 200 OK with DTO")
    void get_ok() throws Exception {
        when(userService.get(1L)).thenReturn(mkUserDto(1L, "x@y.com"));

        mvc.perform(get(BASE + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("x@y.com"));
    }

    @Test
    @DisplayName("GET /api/users -> 200 OK with page of DTOs (service returns Page<User>, controller maps via UserMapper)")
    void getAll_ok() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        User u1 = mkUser(1L, "a@a.com");
        User u2 = mkUser(2L, "b@b.com");
        Page<User> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(userService.getAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.toDto(u1)).thenReturn(mkUserDto(1L, "a@a.com"));
        when(userMapper.toDto(u2)).thenReturn(mkUserDto(2L, "b@b.com"));

        mvc.perform(get(BASE).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].email").value("a@a.com"))
                .andExpect(jsonPath("$.content[1].email").value("b@b.com"));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/cards -> 200 OK with card list")
    void getUserCards_ok() throws Exception {
        List<CardDto> cards = List.of(
                mkCardDto(10L, "4000000000000001", "John"),
                mkCardDto(11L, "4000000000000002", "John")
        );
        when(cardService.findAllByUserId(1L)).thenReturn(cards);

        mvc.perform(get(BASE + "/{userId}/cards", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].number").value("4000000000000001"));
    }
}
