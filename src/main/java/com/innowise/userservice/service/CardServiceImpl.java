package com.innowise.userservice.service;

import com.innowise.userservice.DTO.CardDto;
import com.innowise.userservice.DTO.CardMapper;
import com.innowise.userservice.exception.type.ConflictException;
import com.innowise.userservice.exception.type.NotFoundException;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.repository.CardRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private CardRepository cardRepository;
    private CardMapper cardMapper;

    @Override
    public CardDto getByNumber(String number) {
        return cardRepository.findByNumber(number)
                .map(cardMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("Card", "number", number));
    }

    @Override
    public CardDto getById(Long id) {
        return cardRepository.findById(id)
                .map(cardMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("Card", "id", id));
    }

    @Override
    @Transactional
    public CardDto create(CardDto cardDto) {
        Card entity = cardMapper.toEntity(cardDto);
        entity.setId(null);
        try {
            Card saved = cardRepository.save(entity);
            return cardMapper.toDto(saved);
        } catch (DataIntegrityViolationException e) { //todo
            throw new ConflictException(" Already have this Card");
        }

    }
    @Override
    public Page<CardDto> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            cardRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw NotFoundException.of("Card", "id", id);
        }
    }


    @Override
    public Page<CardDto> findByHolder(String holder, Pageable pageable) {
        return cardRepository.findByHolderContainingIgnoreCase(holder, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    public List<CardDto> findAllByUserId(Long userId) {
        return cardRepository.findAllByUserIdNative(userId).stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CardDto updateBasicInfo(Long id, String holder, LocalDate expirationDate) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Card", "id", id));
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        return cardMapper.toDto(cardRepository.save(card));
    }
    @Override
    @Transactional
    public void deleteByNumber(String number) {
        long deleted = cardRepository.deleteByNumber(number);
        if (deleted == 0) {
            throw NotFoundException.of("Card", "number", number);

        }
    }


}