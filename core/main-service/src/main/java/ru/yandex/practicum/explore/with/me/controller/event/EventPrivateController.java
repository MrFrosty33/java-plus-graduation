package ru.yandex.practicum.explore.with.me.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventFullDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.NewEventDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestDto;
import ru.yandex.practicum.explore.with.me.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPrivateController {
    private final String className = this.getClass().getSimpleName();
    private final EventService eventsService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @PositiveOrZero @NotNull Long userId,
                                    @RequestBody @Valid NewEventDto event) {
        log.trace("{}: createEvent() call with userId: {}, event: {}", className, userId, event);
        return eventsService.createEvent(userId, event);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long userId,
                                     @PathVariable @PositiveOrZero @NotNull Long eventId) {
        log.trace("{}: getEventById() call with userId: {}, eventId: {}", className, userId, eventId);
        return eventsService.getPrivateEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable @PositiveOrZero @NotNull Long userId,
                                    @PathVariable @PositiveOrZero @NotNull Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        log.trace("{}: updateEvent() call with userId: {}, eventId: {}, updateEvent: {}",
                className, userId, eventId, updateEvent);
        return eventsService.updateEvent(userId, eventId, updateEvent);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@PathVariable @PositiveOrZero @NotNull Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getEvents() call with userId: {}, from: {}, size: {}", className, userId, from, size);
        return eventsService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventParticipationRequestsByUser(@PathVariable @PositiveOrZero @NotNull Long userId,
                                                                             @PathVariable @PositiveOrZero @NotNull Long eventId) {
        log.trace("{}: getEventParticipationRequestsByUser() call with userId: {}, eventId: {}",
                className, userId, eventId);
        return eventsService.getEventParticipationRequestsByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable @PositiveOrZero @NotNull Long userId,
                                                                   @PathVariable @PositiveOrZero @NotNull Long eventId,
                                                                   @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        log.trace("{}: getEventParticipationRequestsByUser() call with userId: {}, eventId: {}, updateRequest: {}",
                className, userId, eventId, updateRequest);
        return eventsService.updateEventRequestStatus(userId, eventId, updateRequest);
    }
}
