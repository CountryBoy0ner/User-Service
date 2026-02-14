package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserMapper;
import com.innowise.userservice.exception.type.BadRequestException;
import com.innowise.userservice.exception.type.ConflictException;
import com.innowise.userservice.exception.type.NotFoundException;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.innowise.userservice.service.cache.UserPageCache;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "users")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final UserPageCache userPageCache;


    @Override
    @Transactional
    @CacheEvict(cacheNames = "userLists", allEntries = true)
    public UserDto create(UserDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID must be provided for creation");
        }
        if (userRepo.existsById(dto.getId())) {
            throw new IllegalArgumentException("User with this ID already exists");
        }
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("User with this Email already exists");
        }
        User user = userMapper.toEntity(dto);
        user.setId(dto.getId());
        User saved = userRepo.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(key = "#id")
            },
            evict = {
                    @CacheEvict(cacheNames = "userLists", allEntries = true)
            }
    )
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
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(cacheNames = "userLists", allEntries = true)
    })
    public void delete(Long id) {
        try {
            userRepo.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw NotFoundException.of("User", "id", id);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "users", allEntries = true),
            @CacheEvict(cacheNames = "userLists", allEntries = true)
    })
    public void deleteByEmail(String email) {
        int affected = userRepo.deleteUserByEmail(email);
        if (affected == 0) {
            throw NotFoundException.of("User", "email", email);
        }
    }

    @Override
    @Cacheable(key = "#id")
    public UserDto get(Long id) {
        return userRepo.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));
    }

    @Override
    public Page<UserDto> getAll(Pageable pageable) {
        return userPageCache.getAllCached(pageable).toPage(pageable);
    }


    @Transactional
    @Override
    public UserDto getByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("User", "email", email));
    }

    @Override
    public UserDto getMe(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));
        return userMapper.toDto(user);
    }


    private boolean patchIsEmpty(UserDto patch) {
        return patch.getName() == null
                && patch.getSurname() == null
                && patch.getBirthDate() == null
                && patch.getEmail() == null;
    }
}
