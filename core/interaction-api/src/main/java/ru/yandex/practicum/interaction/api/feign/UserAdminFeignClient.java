package ru.yandex.practicum.interaction.api.feign;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.interaction.api.feign.fallback.UserAdminFallback;
import ru.yandex.practicum.interaction.api.model.user.UserDto;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "user-service", fallback = UserAdminFallback.class)
public interface UserAdminFeignClient {
    //todo fix mapping
    @GetMapping
    List<UserDto> find(@RequestParam(required = false)
                       List<Long> ids,
                       @RequestParam(defaultValue = "0")
                       @PositiveOrZero(message = "must be positive or zero")
                       int from,
                       @RequestParam(defaultValue = "10")
                       @Positive(message = "must be positive")
                       int size,
                       HttpServletRequest request);

    @GetMapping
    Optional<UserDto> findById(@RequestParam @Positive Long id);
}
