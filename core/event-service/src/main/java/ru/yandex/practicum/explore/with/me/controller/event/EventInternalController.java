package ru.yandex.practicum.explore.with.me.controller.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.service.event.EventService;
import ru.yandex.practicum.explore.with.me.stats.StatsSaver;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
@Validated
@Slf4j
public class EventInternalController {
    private final String className = this.getClass().getSimpleName();
    private final EventService service;
    private final StatsSaver statsSaver;

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                     HttpServletRequest request) {
        statsSaver.save(request, className);
        log.trace("{}: getEventById() call with eventId: {}", className, eventId);
        return service.getInternalEventById(eventId);
    }
}
