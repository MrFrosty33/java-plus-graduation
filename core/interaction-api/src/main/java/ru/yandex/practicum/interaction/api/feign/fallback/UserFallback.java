package ru.yandex.practicum.interaction.api.feign.fallback;

import ru.yandex.practicum.interaction.api.feign.UserClient;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

import java.util.Optional;

public class UserFallback implements UserClient {
    @Override
    public Optional<UserDto> findById(Long id) {
        return Optional.empty();
    }
}
