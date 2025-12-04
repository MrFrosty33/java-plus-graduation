package ru.yandex.practicum.interaction.api.feign;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.interaction.api.feign.fallback.EventFallback;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;

@FeignClient(name = "event-service", fallback = EventFallback.class)
public interface EventClient {

    // EventPublicController не может его имплементировать, т.к. принимает ещё и HttpServletRequest, который в фейне я не могу передать
    @GetMapping("/internal/events/{eventId}")
    EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long eventId);
}
