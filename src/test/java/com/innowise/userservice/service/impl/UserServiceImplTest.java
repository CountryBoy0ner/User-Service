package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserMapper;
import com.innowise.userservice.exception.type.BadRequestException;
import com.innowise.userservice.exception.type.ConflictException;
import com.innowise.userservice.exception.type.NotFoundException;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl service;

    private User mkUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setName("John");
        u.setSurname("Doe");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setEmail(email);
        return u;
    }

    private UserDto mkDto(Long id, String email) {
        UserDto d = new UserDto();
        d.setId(id);
        d.setName("John");
        d.setSurname("Doe");
        d.setBirthDate(LocalDate.of(1990, 1, 1));
        d.setEmail(email);
        return d;
    }

    @Test
    @DisplayName("create: saves and returns DTO")
    void create_success() {
        UserDto dto = mkDto(null, "john@site.com");
        User entity = mkUser(null, "john@site.com");

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepo.save(entity)).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(userMapper.toDto(any(User.class))).thenReturn(mkDto(1L, "john@site.com"));

        UserDto result = service.create(dto);

        assertEquals(1L, result.getId());
        assertEquals("john@site.com", result.getEmail());
        verify(userRepo).save(entity);
    }

    @Test
    @DisplayName("create: unique email violation -> ConflictException")
    void create_conflict() {
        UserDto dto = mkDto(null, "dup@site.com");
        User entity = mkUser(null, "dup@site.com");

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepo.save(entity)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(ConflictException.class, () -> service.create(dto));
    }

    @Test
    @DisplayName("patch: updates fields and returns DTO")
    void patch_success() {
        User existing = mkUser(1L, "old@site.com");
        UserDto patch = new UserDto();
        patch.setName("NewName");
        patch.setEmail("new@site.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(existing));
        // mapper updates entity in-place
        doAnswer(inv -> {
            UserDto p = inv.getArgument(0);
            User e = inv.getArgument(1);
            if (p.getName() != null) e.setName(p.getName());
            if (p.getEmail() != null) e.setEmail(p.getEmail());
            return null;
        }).when(userMapper).updateEntity(eq(patch), eq(existing));

        when(userRepo.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(mkDto(1L, "new@site.com"));

        UserDto result = service.patch(1L, patch);

        assertEquals("NewName", existing.getName());
        assertEquals("new@site.com", existing.getEmail());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("patch: throws NotFoundException when user does not exist")
    void patch_notFound() {
        when(userRepo.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.patch(100L, new UserDto()));
    }

    @Test
    @DisplayName("patch: throws BadRequestException when no fields provided")
    void patch_badRequest_whenEmpty() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mkUser(1L, "a@a.com")));
        UserDto empty = new UserDto();
        assertThrows(BadRequestException.class, () -> service.patch(1L, empty));
    }

    @Test
    @DisplayName("patch: throws ConflictException on unique constraint violation")
    void patch_conflict() {
        User existing = mkUser(1L, "old@site.com");
        UserDto patch = new UserDto();
        patch.setEmail("new@site.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(userMapper).updateEntity(eq(patch), eq(existing));
        when(userRepo.save(existing)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(ConflictException.class, () -> service.patch(1L, patch));
    }

    @Test
    @DisplayName("delete: deletes by id")
    void delete_success() {
        doNothing().when(userRepo).deleteById(1L);
        assertDoesNotThrow(() -> service.delete(1L));
        verify(userRepo).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws NotFoundException when user is missing")
    void delete_notFound() {
        doThrow(new EmptyResultDataAccessException(1)).when(userRepo).deleteById(99L);
        assertThrows(NotFoundException.class, () -> service.delete(99L));
    }

    @Test
    @DisplayName("deleteByEmail: deletes when at least one row is affected")
    void deleteByEmail_success() {
        when(userRepo.deleteUserByEmail("john@site.com")).thenReturn(1);
        assertDoesNotThrow(() -> service.deleteByEmail("john@site.com"));
        verify(userRepo).deleteUserByEmail("john@site.com");
    }

    @Test
    @DisplayName("deleteByEmail: throws NotFoundException when zero rows were deleted")
    void deleteByEmail_notFound() {
        when(userRepo.deleteUserByEmail("none@site.com")).thenReturn(0);
        assertThrows(NotFoundException.class, () -> service.deleteByEmail("none@site.com"));
    }

    @Test
    @DisplayName("get: returns DTO by id")
    void get_success() {
        User u = mkUser(1L, "x@y.com");
        when(userRepo.findById(1L)).thenReturn(Optional.of(u));
        when(userMapper.toDto(u)).thenReturn(mkDto(1L, "x@y.com"));

        UserDto result = service.get(1L);
        assertEquals(1L, result.getId());
        assertEquals("x@y.com", result.getEmail());
    }

    @Test
    @DisplayName("get: throws NotFoundException when user is absent")
    void get_notFound() {
        when(userRepo.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(404L));
    }

    @Test
    @DisplayName("getAll: returns a page of users")
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 2);
        User u1 = mkUser(1L, "a@a.com");
        User u2 = mkUser(2L, "b@b.com");
        when(userRepo.findAllUsers(pageable)).thenReturn(new PageImpl<>(List.of(u1, u2), pageable, 2));

        Page<User> page = service.getAll(pageable);
        assertEquals(2, page.getTotalElements());
        assertEquals(List.of(u1, u2), page.getContent());
    }


    @Test
    @DisplayName("getByEmail: returns DTO when user exists")
    void getByEmail_success() {
        String email = "x@y.com";
        User user = mkUser(1L, email);
        UserDto dto = mkDto(1L, email);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = service.getByEmail(email);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(email, result.getEmail());
        verify(userRepo).findByEmail(email);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("getByEmail: throws NotFoundException when user is absent")
    void getByEmail_notFound() {
        String email = "none@site.com";

        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByEmail(email));
        verify(userRepo).findByEmail(email);
        verifyNoInteractions(userMapper);
    }

}
