package com.innowise.userservice.DTO;

import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface  UserMapper {

    UserDto toDto(User entity);
    List<UserDto> toDtoList(List<User> entities);


    User toEntity(UserDto dto);
    List<User> toEntityList(List<UserDto> dtos);



    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)/// PATCH
    void updateEntity(UserDto dto, @MappingTarget User entity);

    @AfterMapping
    default void linkCards(@MappingTarget User user) {
        if (user.getCards() != null) {
            for (Card c : user.getCards()) {
                c.setUser(user);
            }
        }
    }
}
