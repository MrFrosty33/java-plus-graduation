package ru.yandex.practicum.interaction.api.feign;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.interaction.api.feign.fallback.UserFallback;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

@FeignClient(name = "user-service", fallback = UserFallback.class)
public interface UserClient {
    @GetMapping("/internal/users")
    UserDto findById(@RequestParam @Positive Long id);
}
