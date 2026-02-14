package com.innowise.userservice.controller;

import com.innowise.userservice.config.CustomUser;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserMapper;
import com.innowise.userservice.dto.ValidationGroups;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final CardService cardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody UserDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> patch(@PathVariable Long id, @Validated(ValidationGroups.OnPatch.class) @RequestBody UserDto patch) {
        UserDto updated = userService.patch(id, patch);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteByEmail(@RequestParam @NotBlank @Email String email) {
        userService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.get(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<CardDto>> getUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.findAllByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getByEmail(@RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> me(
            @RequestHeader("X-User-Id") Long id
    ) {
        return ResponseEntity.ok(userService.getMe(id));
    }
}
