package com.innowise.userservice.service;

import com.innowise.userservice.DTO.UserDto;
import com.innowise.userservice.DTO.UserMapper;
import com.innowise.userservice.exception.type.BadRequestException;
import com.innowise.userservice.exception.type.ConflictException;
import com.innowise.userservice.exception.type.NotFoundException;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        User entity = userMapper.toEntity(dto);
        try {
            User saved = userRepo.save(entity);
            return userMapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("User violates database constraints (likely duplicate email)");
        }
    }

    @Override
    @Transactional
    public UserDto patch(Long id, UserDto patch) {
        User entity = userRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));
        if (patchIsEmpty(patch)) {
            throw new BadRequestException("No fields to update");
        }
        userMapper.updateEntity(patch, entity);
        try {
            User saved = userRepo.save(entity);
            return userMapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("User update violates database constraints (likely duplicate email)");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            userRepo.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw NotFoundException.of("User", "id", id);
        }
    }

    @Override
    @Transactional
    public void deleteByEmail(String email) {
        int affected = userRepo.deleteUserByEmail(email);
        if (affected == 0) {
            throw NotFoundException.of("User", "email", email);
        }
    }

    @Override
    public UserDto get(Long id) {
        return userRepo.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        return userRepo.findAllUsers(pageable);
    }

    private boolean patchIsEmpty(UserDto patch) {
        return patch.getName() == null
                && patch.getSurname() == null
                && patch.getBirthDate() == null
                && patch.getEmail() == null;
    }
}
