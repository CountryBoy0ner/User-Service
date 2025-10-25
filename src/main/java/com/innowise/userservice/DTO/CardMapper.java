package com.innowise.userservice.DTO;

import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card entity);

    List<CardDto> toDtoList(List<Card> entities);


    @Mapping(target = "user", source = "userId")
    Card toEntity(CardDto dto);

    List<Card> toEntityList(List<CardDto> dtos);


    default User map(Long userId) {
        if (userId == null) return null;
        User u = new User();
        u.setId(userId);
        return u;
    }

    default Long map(User user) {
        return user != null ? user.getId() : null;
    }
}
