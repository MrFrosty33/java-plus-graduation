package ru.yandex.practicum.user.service;

import ru.yandex.practicum.interaction.api.model.user.AdminUserFindParam;
import ru.yandex.practicum.interaction.api.model.user.NewUserRequest;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> find(AdminUserFindParam param);

    Optional<UserDto> findById(Long id);

    UserDto create(NewUserRequest newUserRequest);

    void delete(Long userId);
}
