package com.innowise.userservice.service;

import com.innowise.userservice.DTO.CardDto;
import com.innowise.userservice.DTO.CardMapper;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface CardService {

    public CardDto getByNumber(String number);

    public CardDto getById(Long id);
    public CardDto create(CardDto cardDto);

    public Page<CardDto> getAll(Pageable pageable);

    public void delete(Long id);

    public Page<CardDto> findByHolder(String holder, Pageable pageable);
    public List<CardDto> findAllByUserId(Long userId);

    public CardDto updateBasicInfo(Long id, String holder, LocalDate expirationDate);

    public void deleteByNumber(String number);

}
