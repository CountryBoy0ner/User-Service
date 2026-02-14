package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserDto create(UserDto dto);

    UserDto patch(Long id, UserDto patch);

    void delete(Long id);

    void deleteByEmail(String email);

    UserDto get(Long id);

    Page<UserDto> getAll(Pageable pageable);

    UserDto getByEmail(String email);

    UserDto getMe(Long id);
}

