package com.innowise.userservice.controller;

import com.innowise.userservice.DTO.CardDto;
import com.innowise.userservice.DTO.UserDto;
import com.innowise.userservice.DTO.UserMapper;
import com.innowise.userservice.DTO.ValidationGroups;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<UserDto> create(
            @Validated(ValidationGroups.OnCreate.class) @RequestBody UserDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patch(@PathVariable Long id,
                                         @Validated(ValidationGroups.OnPatch.class) @RequestBody UserDto patch) {
        UserDto updated = userService.patch(id, patch);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-email")
    public ResponseEntity<Void> deleteByEmail(@RequestParam @NotBlank @Email String email) {
        userService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UserDto> page = userService.getAll(pageable).map(userMapper::toDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.findAllByUserId(userId));
    }
}
