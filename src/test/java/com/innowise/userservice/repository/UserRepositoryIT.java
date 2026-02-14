package com.innowise.userservice.repository;

import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
class UserRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_service")
            .withUsername("postgres")
            .withPassword("123");

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    private static long idCounter = 1;

    private User mkUser(String name, String surname, String email) {
        User u = new User();
        u.setId(generateTestId()); // manual id
        u.setName(name);
        u.setSurname(surname);
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setEmail(email);
        return u;
    }

    private Card mkCard(String number, String holder, User owner) {
        Card c = new Card();
        c.setNumber(number);
        c.setHolder(holder);
        c.setExpirationDate(LocalDate.now().plusYears(3));
        c.setUser(owner);
        return c;
    }

    @Test
    @DisplayName("findByEmail: returns user when present")
    void findByEmail_success() {
        User saved = userRepository.save(mkUser("John", "Doe", "john@example.com"));

        assertTrue(userRepository.findByEmail("john@example.com").isPresent());
        assertFalse(userRepository.findByEmail("nope@example.com").isPresent());
        assertEquals(saved.getId(),
                userRepository.findByEmail("john@example.com").orElseThrow().getId());
    }

    @Test
    @DisplayName("searchUsersByName: finds by LIKE and ordered by surname ASC")
    void searchUsersByName_success() {
        userRepository.saveAll(Arrays.asList(
                mkUser("John", "Zeta", "z@mail.com"),
                mkUser("Johnny", "Alpha", "a@mail.com"),
                mkUser("Alice", "Beta", "b@mail.com")
        ));

        List<User> result = userRepository.searchUsersByName("John");

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).getSurname());
        assertEquals("Zeta", result.get(1).getSurname());
    }

    @Test
    @DisplayName("findUserByCardNumber: native join users ↔ card_info by card number")
    void findUserByCardNumber_success() {
        User owner = userRepository.save(mkUser("Owner", "One", "owner@mail.com"));
        Card card = cardRepository.save(mkCard("4111111111111234", "Owner One", owner));

        assertTrue(userRepository.findUserByCardNumber(card.getNumber()).isPresent());
        assertEquals(owner.getId(),
                userRepository.findUserByCardNumber("4111111111111234").orElseThrow().getId());
        assertTrue(userRepository.findUserByCardNumber("0000").isEmpty());
    }

    @Test
    @DisplayName("findAllUsers(Pageable): returns page with total count")
    void findAllUsers_pageable() {
        userRepository.saveAll(Arrays.asList(
                mkUser("U1", "S1", "u1@mail.com"),
                mkUser("U2", "S2", "u2@mail.com"),
                mkUser("U3", "S3", "u3@mail.com")
        ));

        Page<User> page0 = userRepository.findAllUsers(PageRequest.of(0, 2));
        assertEquals(3, page0.getTotalElements());
        assertEquals(2, page0.getContent().size());

        Page<User> page1 = userRepository.findAllUsers(PageRequest.of(1, 2));
        assertEquals(1, page1.getContent().size());
    }

    @Test
    @DisplayName("updateUserInfo: updates name, surname and email by id")
    void updateUserInfo_success() {
        User saved = userRepository.save(mkUser("Old", "Name", "old@mail.com"));

        int affected = userRepository.updateUserInfo(
                saved.getId(),
                "New",
                "Surname",
                "new@mail.com"
        );

        assertEquals(1, affected);

        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("New", updated.getName());
        assertEquals("Surname", updated.getSurname());
        assertEquals("new@mail.com", updated.getEmail());
    }

    @Test
    @DisplayName("deleteUserByEmail: native delete returns number of rows")
    void deleteUserByEmail_success() {
        userRepository.save(mkUser("Del", "Me", "del@mail.com"));

        int affected = userRepository.deleteUserByEmail("del@mail.com");
        assertEquals(1, affected);
        assertTrue(userRepository.findByEmail("del@mail.com").isEmpty());
    }

    private synchronized long generateTestId() {
        return idCounter++;
    }
}