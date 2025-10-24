package com.innowise.userservice.service;

import com.innowise.userservice.DTO.UserDto;
import com.innowise.userservice.DTO.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepo;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(UserDto dto) {
        User entity = userMapper.toEntity(dto);
        userRepo.save(entity);
        return userMapper.toDto(entity);
    }
    @Transactional
    public UserDto patch(Long id, UserDto patch) {
        User entity = userRepo.findById(id).orElseThrow();
        userMapper.updateEntity(patch, entity);
        return userMapper.toDto(entity);
    }
    @Transactional
    public void delete(Long id) {
        userRepo.deleteById(id);
    }


    @Transactional
    public void deleteByEmail(String email){
        userRepo.deleteUserByEmail(email);
    }


    //readOnly
    public UserDto get(Long id) {
        return userRepo.findById(id).map(userMapper::toDto).orElseThrow();
    }

    public Page<User> getAll(Pageable pageable){
        return userRepo.findAllUsers(pageable);
    }
}
