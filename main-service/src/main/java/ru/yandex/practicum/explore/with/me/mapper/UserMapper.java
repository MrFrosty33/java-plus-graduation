package ru.yandex.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.explore.with.me.model.user.NewUserRequest;
import ru.yandex.practicum.explore.with.me.model.user.User;
import ru.yandex.practicum.explore.with.me.model.user.UserDto;
import ru.yandex.practicum.explore.with.me.model.user.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User entity);

    UserShortDto toShortDto(User entity);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);
}