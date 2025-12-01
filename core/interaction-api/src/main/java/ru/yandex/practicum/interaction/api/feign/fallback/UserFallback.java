package ru.yandex.practicum.interaction.api.feign.fallback;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.practicum.interaction.api.feign.UserFeignClient;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

import java.util.List;
import java.util.Optional;

public class UserFallback implements UserFeignClient {
    @Override
    public List<UserDto> find(List<Long> ids, int from, int size, HttpServletRequest request) {
        return List.of();
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return Optional.empty();
    }
}
