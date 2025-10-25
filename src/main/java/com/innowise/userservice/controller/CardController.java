package com.innowise.userservice.controller;

import com.innowise.userservice.DTO.CardDto;
import com.innowise.userservice.DTO.ValidationGroups;
import com.innowise.userservice.service.CardService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CardDto> create(
            @Validated(ValidationGroups.OnCreate.class) @RequestBody CardDto dto) {
        CardDto created = cardService.create(dto);
        return ResponseEntity.created(URI.create("/api/cards/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping("/by-number/{number}")
    public ResponseEntity<CardDto> getByNumber(
            @PathVariable @NotBlank @Size(min = 12, max = 19) String number) {
        return ResponseEntity.ok(cardService.getByNumber(number));
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }

    @GetMapping("/by-holder")
    public ResponseEntity<Page<CardDto>> findByHolder(
            @RequestParam("holder") @NotBlank String holder,
            @PageableDefault(size = 15, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(cardService.findByHolder(holder, pageable));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<CardDto>> findAllByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.findAllByUserId(userId));
    }

    @PutMapping("/{id}/basic-info")
    public ResponseEntity<CardDto> updateBasicInfo(@PathVariable Long id,
                                                   @Validated(ValidationGroups.OnUpdateBasicInfo.class) @RequestBody CardDto dto) {
        CardDto updated = cardService.updateBasicInfo(id, dto.getHolder(), dto.getExpirationDate());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-number/{number}")
    public ResponseEntity<Void> deleteByNumber(
            @PathVariable @NotBlank @Size(min = 12, max = 19) String number) {
        cardService.deleteByNumber(number);
        return ResponseEntity.noContent().build();
    }
}
