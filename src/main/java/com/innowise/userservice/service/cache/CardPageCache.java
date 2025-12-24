package com.innowise.userservice.service.cache;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardPageCache {

    private final CardRepository repo;
    private final CardMapper mapper;

    @Cacheable(
            cacheNames = "cardLists",
            key = "'p=' + #pageable.pageNumber + ':s=' + #pageable.pageSize + ':sort=' + #pageable.sort",
            condition = "#pageable.pageSize <= 100"
    )
    public CachedPage<CardDto> getAllCached(Pageable pageable) {
        return CachedPage.from(repo.findAll(pageable).map(mapper::toDto));
    }

    @Cacheable(
            cacheNames = "cardsByHolder",
            key = "'h=' + #holder.toLowerCase() + ':p=' + #pageable.pageNumber + ':s=' + #pageable.pageSize + ':sort=' + #pageable.sort",
            condition = "#pageable.pageSize <= 100"
    )
    public CachedPage<CardDto> findByHolderCached(String holder, Pageable pageable) {
        return CachedPage.from(repo.findByHolderContainingIgnoreCase(holder, pageable).map(mapper::toDto));
    }
}
