package com.innowise.userservice.service.cache;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPageCache {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Cacheable(
            cacheNames = "userLists",
            key = "'p=' + #pageable.pageNumber + ':s=' + #pageable.pageSize + ':sort=' + #pageable.sort",
            condition = "#pageable.pageSize <= 100"
    )
    public CachedPage<UserDto> getAllCached(Pageable pageable) {
        return CachedPage.from(repo.findAllUsers(pageable).map(mapper::toDto));
    }
}
