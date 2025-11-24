package ru.yandex.practicum.explore.with.me.controller.participation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.participation.CancelParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.NewParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestDto;
import ru.yandex.practicum.explore.with.me.service.participation.request.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@Validated
public class ParticipationRequestPrivateController {
    private final ParticipationRequestService service;
    private final String controllerName = this.getClass().getSimpleName();

    @GetMapping
    public List<ParticipationRequestDto> find(@PathVariable
                                              @NotNull(message = "must not be null")
                                              @PositiveOrZero(message = "must be positive or zero")
                                              Long userId) {
        log.trace("{}: find() call with userId: {}", controllerName, userId);
        return service.find(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long userId,
                                          @RequestParam
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long eventId) {
        log.trace("{}: create() call with userId: {}, eventId: {}", controllerName, userId, eventId);

        NewParticipationRequest newParticipationRequest = NewParticipationRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .build();
        return service.create(newParticipationRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long userId,
                                          @PathVariable
                                          @NotNull(message = "must not be null")
                                          @PositiveOrZero(message = "must be positive or zero")
                                          Long requestId) {
        log.trace("{}: cancel() call with userId: {}, requestId: {}", controllerName, userId, requestId);

        CancelParticipationRequest cancelParticipationRequest = CancelParticipationRequest.builder()
                .userId(userId)
                .requestId(requestId)
                .build();
        return service.cancel(cancelParticipationRequest);
    }
}
