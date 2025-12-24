package com.innowise.userservice.repository;

import com.innowise.userservice.it.ContainersConfig;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@Import(ContainersConfig.class)
@AutoConfigureTestDatabase(replace = NONE)
class CardRepositoryIT {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User mkUser(String email) {
        User u = new User();
        u.setName("John");
        u.setSurname("Doe");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setEmail(email);
        return userRepository.save(u);
    }

    private Card mkCard(String number, String holder, LocalDate exp, User owner) {
        Card c = new Card();
        c.setNumber(number);
        c.setHolder(holder);
        c.setExpirationDate(exp);
        c.setUser(owner); // @ManyToOne
        return c;
    }

    @Test
    @DisplayName("findByNumber: returns card when present")
    void findByNumber_success() {
        User u = mkUser("u1@mail.com");
        Card saved = cardRepository.save(
                mkCard("4111111111111111", "John Doe", LocalDate.now().plusYears(3), u)
        );

        assertTrue(cardRepository.findByNumber("4111111111111111").isPresent());
        assertEquals(saved.getId(), cardRepository.findByNumber("4111111111111111").orElseThrow().getId());
        assertTrue(cardRepository.findByNumber("0000").isEmpty());
    }

    @Test
    @DisplayName("findByHolderContainingIgnoreCase: returns a page")
    void findByHolder_page() {
        User u = mkUser("u2@mail.com");
        cardRepository.save(mkCard("4000000000000001", "Alice Smith", LocalDate.now().plusYears(3), u));
        cardRepository.save(mkCard("4000000000000002", "ALICE Johnson", LocalDate.now().plusYears(4), u));
        cardRepository.save(mkCard("4000000000000003", "Bob Brown", LocalDate.now().plusYears(5), u));

        Page<Card> page = cardRepository.findByHolderContainingIgnoreCase("alice", PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(c -> c.getHolder().toLowerCase().contains("alice")));
    }

    @Test
    @DisplayName("findAllByUserIdNative: returns only cards for given user")
    void findAllByUserIdNative_success() {
        User u1 = mkUser("owner1@mail.com");
        User u2 = mkUser("owner2@mail.com");

        cardRepository.save(mkCard("4111111111111234", "Owner One", LocalDate.now().plusYears(3), u1));
        cardRepository.save(mkCard("5555555555554444", "Owner One 2", LocalDate.now().plusYears(4), u1));
        cardRepository.save(mkCard("4222222222222222", "Owner Two", LocalDate.now().plusYears(5), u2));

        List<Card> forU1 = cardRepository.findAllByUserIdNative(u1.getId());
        assertEquals(2, forU1.size());

        List<Card> forU2 = cardRepository.findAllByUserIdNative(u2.getId());
        assertEquals(1, forU2.size());
    }

    @Test
    @DisplayName("updateCardInfo: updates holder and expirationDate by id")
    void updateCardInfo_success() {
        User u = mkUser("u3@mail.com");
        Card saved = cardRepository.save(
                mkCard("4000000000000099", "Old Holder", LocalDate.now().plusYears(2), u)
        );

        // Репозиторий принимает expirationDate как String — передадим ISO-дату
        int affected = cardRepository.updateCardInfo(
                saved.getId(),
                "New Holder",
                LocalDate.of(2035, 5, 31).toString() // "2035-05-31"
        );
        assertEquals(1, affected);

        Card reloaded = cardRepository.findById(saved.getId()).orElseThrow();
        assertEquals("New Holder", reloaded.getHolder());
        assertEquals(LocalDate.of(2035, 5, 31), reloaded.getExpirationDate());
    }

    @Test
    @DisplayName("deleteByNumber: returns 1 when deleted and 0 when nothing to delete")
    void deleteByNumber_success_and_notFound() {
        User u = mkUser("u4@mail.com");
        cardRepository.save(mkCard("4999999999999999", "Del Me", LocalDate.now().plusYears(3), u));

        long affected = cardRepository.deleteByNumber("4999999999999999");
        assertEquals(1L, affected);
        assertTrue(cardRepository.findByNumber("4999999999999999").isEmpty());

        long zero = cardRepository.deleteByNumber("0000");
        assertEquals(0L, zero);
    }
}
