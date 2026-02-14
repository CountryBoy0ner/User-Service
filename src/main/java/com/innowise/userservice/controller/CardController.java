package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.ValidationGroups;
import com.innowise.userservice.service.CardService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Validated
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CardDto> create(
            @Validated(ValidationGroups.OnCreate.class) @RequestBody CardDto dto,
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        if (!currentUserId.equals(dto.getUserId())) {
            throw new RuntimeException("You can only create cards for yourself");
        }
        CardDto created = cardService.create(dto);
        return ResponseEntity.created(URI.create("/api/cards/" + created.getId()))
                .body(created);
    }

    // Админские методы с @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping("/by-number/{number}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getByNumber(
            @PathVariable @NotBlank @Size(min = 12, max = 19) String number) {
        return ResponseEntity.ok(cardService.getByNumber(number));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }

    @GetMapping("/by-holder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> findByHolder(
            @RequestParam("holder") @NotBlank String holder,
            @PageableDefault(size = 15, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(cardService.findByHolder(holder, pageable));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> findAllByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.findAllByUserId(userId));
    }

    @PutMapping("/{id}/basic-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateBasicInfo(@PathVariable Long id,
                                                   @Validated(ValidationGroups.OnUpdateBasicInfo.class) @RequestBody CardDto dto) {
        CardDto updated = cardService.updateBasicInfo(id, dto.getHolder(), dto.getExpirationDate());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-number/{number}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteByNumber(
            @PathVariable @NotBlank @Size(min = 12, max = 19) String number) {
        cardService.deleteByNumber(number);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CardDto>> getMyCards(
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        return ResponseEntity.ok(cardService.findAllByUserId(currentUserId));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CardDto> getMyCard(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        CardDto card = cardService.getById(id);
        if (!currentUserId.equals(card.getUserId())) {
            throw new RuntimeException("You can only access your own cards");
        }
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyCard(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        CardDto card = cardService.getById(id);
        if (!currentUserId.equals(card.getUserId())) {
            throw new RuntimeException("You can only delete your own cards");
        }
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

}