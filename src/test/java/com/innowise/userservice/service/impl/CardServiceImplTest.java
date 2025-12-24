package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CardMapper;
import com.innowise.userservice.exception.type.ConflictException;
import com.innowise.userservice.exception.type.NotFoundException;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.service.cache.CachedPage;
import com.innowise.userservice.service.cache.CardPageCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl service;


    @Mock
    private CardPageCache cardPageCache;


    private Card card;
    private CardDto dto;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId(1L);
        card.setNumber("4111111111111111");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.of(2030, 1, 31));

        dto = new CardDto();
        dto.setId(1L);
        dto.setNumber("4111111111111111");
        dto.setHolder("John Doe");
        dto.setExpirationDate(LocalDate.of(2030, 1, 31));
        dto.setUserId(10L);
    }

    @Test
    @DisplayName("getByNumber: returns DTO when card exists")
    void getByNumber_found() {
        when(cardRepository.findByNumber("4111111111111111")).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(dto);

        CardDto result = service.getByNumber("4111111111111111");

        assertEquals(dto, result);
        verify(cardRepository).findByNumber("4111111111111111");
        verify(cardMapper).toDto(card);
    }

    @Test
    @DisplayName("getByNumber: throws NotFoundException when card is absent")
    void getByNumber_notFound() {
        when(cardRepository.findByNumber("0000")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getByNumber("0000"));
    }

    @Test
    @DisplayName("getById: returns DTO when card exists")
    void getById_found() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(dto);

        CardDto result = service.getById(1L);

        assertEquals(dto, result);
        verify(cardRepository).findById(1L);
    }

    @Test
    @DisplayName("getById: throws NotFoundException when card is absent")
    void getById_notFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(99L));
    }

    @Test
    @DisplayName("create: saves a new card and maps to DTO")
    void create_success() {
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);

        // Mapper returns a NEW entity without id (so id must be null BEFORE save)
        Card entityToSave = new Card();
        entityToSave.setNumber("4111111111111111");
        entityToSave.setHolder("John Doe");
        entityToSave.setExpirationDate(LocalDate.of(2030, 1, 31));

        when(cardMapper.toEntity(dto)).thenReturn(entityToSave);

        // IMPORTANT: do NOT mutate the same instance; return a new "saved" entity
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card incoming = inv.getArgument(0);
            // (optional) assert precondition right here
            assertNull(incoming.getId(), "id should be null before save()");
            Card saved = new Card();
            saved.setId(1L);
            saved.setNumber(incoming.getNumber());
            saved.setHolder(incoming.getHolder());
            saved.setExpirationDate(incoming.getExpirationDate());
            return saved;
        });

        when(cardMapper.toDto(any(Card.class))).thenReturn(dto);

        CardDto result = service.create(dto);

        assertEquals(dto, result);
        verify(cardRepository).save(captor.capture());

        // ensure id is null at the moment of persisting a new entity
        assertNull(captor.getValue().getId(), "id should be null before save()");
    }

    @Test
    @DisplayName("create: throws ConflictException on unique constraint violation")
    void create_conflict() {
        when(cardMapper.toEntity(dto)).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenThrow(new DataIntegrityViolationException("dup"));
        assertThrows(ConflictException.class, () -> service.create(dto));
    }

    @Test
    @DisplayName("getAll: returns a page of DTOs")
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 2);

        CardDto d2 = new CardDto();
        d2.setId(2L);
        d2.setNumber("5555555555554444");
        d2.setHolder("Jane Roe");
        d2.setExpirationDate(LocalDate.of(2031, 12, 31));
        d2.setUserId(11L);

        when(cardPageCache.getAllCached(pageable))
                .thenReturn(new CachedPage<>(List.of(dto, d2), 2));

        Page<CardDto> page = service.getAll(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals(List.of(dto, d2), page.getContent());
        verify(cardPageCache).getAllCached(pageable);
        verifyNoInteractions(cardRepository, cardMapper);
    }


    @Test
    @DisplayName("delete: deletes by id")
    void delete_success() {
        doNothing().when(cardRepository).deleteById(1L);
        assertDoesNotThrow(() -> service.delete(1L));
        verify(cardRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws NotFoundException when entity is missing")
    void delete_notFound() {
        doThrow(new EmptyResultDataAccessException(1)).when(cardRepository).deleteById(99L);
        assertThrows(NotFoundException.class, () -> service.delete(99L));
    }

    @Test
    @DisplayName("findByHolder: returns a page of DTOs")
    void findByHolder_success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(cardPageCache.findByHolderCached("john", pageable))
                .thenReturn(new CachedPage<>(List.of(dto), 1));

        Page<CardDto> page = service.findByHolder("john", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(dto, page.getContent().get(0));
        verify(cardPageCache).findByHolderCached("john", pageable);
        verifyNoInteractions(cardRepository, cardMapper);
    }


    @Test
    @DisplayName("findAllByUserId: returns a list of DTOs")
    void findAllByUserId_success() {
        when(cardRepository.findAllByUserIdNative(10L)).thenReturn(List.of(card));
        when(cardMapper.toDto(card)).thenReturn(dto);

        List<CardDto> list = service.findAllByUserId(10L);
        assertEquals(1, list.size());
        assertEquals(dto, list.get(0));
    }

    @Test
    @DisplayName("updateBasicInfo: updates holder and expirationDate")
    void updateBasicInfo_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cardMapper.toDto(any(Card.class))).thenReturn(dto);

        CardDto result = service.updateBasicInfo(1L, "New Holder", LocalDate.of(2035, 5, 31));

        assertEquals(dto, result);
        assertEquals("New Holder", card.getHolder());
        assertEquals(LocalDate.of(2035, 5, 31), card.getExpirationDate());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("updateBasicInfo: throws NotFoundException when card is absent")
    void updateBasicInfo_notFound() {
        when(cardRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.updateBasicInfo(42L, "X", LocalDate.now().plusYears(1)));
    }

    @Test
    @DisplayName("deleteByNumber: deletes by card number")
    void deleteByNumber_success() {
        when(cardRepository.deleteByNumber("4111111111111111")).thenReturn(1L);
        assertDoesNotThrow(() -> service.deleteByNumber("4111111111111111"));
        verify(cardRepository).deleteByNumber("4111111111111111");
    }

    @Test
    @DisplayName("deleteByNumber: throws NotFoundException when nothing was deleted")
    void deleteByNumber_notFound() {
        when(cardRepository.deleteByNumber("0000")).thenReturn(0L);
        assertThrows(NotFoundException.class, () -> service.deleteByNumber("0000"));
    }
}
