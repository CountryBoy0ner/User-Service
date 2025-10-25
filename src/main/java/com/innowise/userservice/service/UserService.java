package com.innowise.userservice.service;

import com.innowise.userservice.DTO.UserDto;
import com.innowise.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    public UserDto create(UserDto dto);

    public UserDto patch(Long id, UserDto patch);

    public void delete(Long id);

    public void deleteByEmail(String email);

    public UserDto get(Long id);

    public Page<User> getAll(Pageable pageable);
}

