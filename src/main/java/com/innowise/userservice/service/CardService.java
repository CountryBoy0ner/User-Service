package com.innowise.userservice.service;

import com.innowise.userservice.DTO.CardDto;
import com.innowise.userservice.DTO.CardMapper;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardDto getByNumber(String number) {
        return cardRepository.findByNumber(number)
                .map(cardMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Card not found by number: " + number));
    }

    public CardDto getById(Long id) {
        return cardRepository.findById(id)
                .map(cardMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Card not found by id: " + id));
    }

    @Transactional
    public CardDto create(CardDto cardDto) {
        Card entity = cardMapper.toEntity(cardDto);
        entity.setId(null);
        try {
            Card saved = cardRepository.save(entity);
            return cardMapper.toDto(saved);
        } catch (DataIntegrityViolationException e) { //todo
            throw e; // throw new DuplicateCardException("Card number already exists");
        }
    }

    public Page<CardDto> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDto);
    }

    @Transactional
    public void delete(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found by id: " + id);
        }
        cardRepository.deleteById(id);
    }

    public Page<CardDto> findByHolder(String holder, Pageable pageable) {
        return cardRepository.findByHolderContainingIgnoreCase(holder, pageable)
                .map(cardMapper::toDto);
    }

    public List<CardDto> findAllByUserId(Long userId) {
        return cardRepository.findAllByUserIdNative(userId).stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Transactional
    public CardDto updateBasicInfo(Long id, String holder, LocalDate expirationDate) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found by id: " + id));
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        return cardMapper.toDto(cardRepository.save(card));
    }

    @Transactional
    public void deleteByNumber(String number) {
        long deleted = cardRepository.deleteByNumber(number);
        if (deleted == 0) {
            throw new EntityNotFoundException("Card not found by number: " + number);
        }
    }
}
