package ru.yandex.practicum.interaction.api.feign.fallback;

import ru.yandex.practicum.interaction.api.feign.UserClient;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

public class UserFallback implements UserClient {
    @Override
    public UserDto findById(Long id) {
        return null;
    }
}
