package ru.yandex.practicum.request.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction.api.feign.RequestInternalFeignClient;
import ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;
import ru.yandex.practicum.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@Validated
public class ParticipationRequestInternalController implements RequestInternalFeignClient {
    private final ParticipationRequestService service;
    private final String controllerName = this.getClass().getSimpleName();

    @GetMapping(params = "requestIds")
    public List<ParticipationRequestDto> findAllById(@RequestParam
                                                     @NotNull
                                                     @NotEmpty
                                                     List<Long> requestIds) {
        log.trace("{}: findAllById() call with requestIds: {}", controllerName, requestIds);
        return service.findAllById(requestIds);
    }

    @GetMapping(params = "eventId")
    public List<ParticipationRequestDto> findAllByEventId(@RequestParam @Positive long eventId) {
        log.trace("{}: findAllByEventId() call with eventId: {}", controllerName, eventId);
        return service.findAllByEventId(eventId);
    }

    @GetMapping(params = {"eventId", "status"})
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(@RequestParam @Positive long eventId,
                                                                   @RequestParam ParticipationRequestStatus status) {
        log.trace("{}: findAllByEventIdAndStatus() call with eventId: {} and status: {}", controllerName, eventId, status);
        return service.findAllByEventIdAndStatus(eventId, status);
    }

    @PostMapping("/status/update")
    public void updateStatus(@RequestParam
                             @NotNull
                             @NotEmpty
                             List<Long> requestIds,
                             ParticipationRequestStatus status) {
        log.trace("{}: updateStatus() call with requestIds: {} and status: {}", controllerName, requestIds, status);
        service.updateStatus(requestIds, status);
    }

    @GetMapping("/count")
    public List<EventRequestCount> countGroupByEventId(@RequestParam
                                                       @NotNull
                                                       @NotEmpty
                                                       List<Long> eventIds) {
        log.trace("{}: countGroupByEventId() call with eventIds: {}", controllerName, eventIds);
        return service.countGroupByEventId(eventIds);
    }

    @GetMapping("/exists")
    public boolean existsByRequesterIdAndEventIdAndStatus(@RequestParam @NotNull @Positive Long requesterId,
                                                          @RequestParam @NotNull @Positive Long eventId,
                                                          ParticipationRequestStatus status) {
        log.trace("{}: existsByRequesterIdAndEventIdAndStatus() call with requesterId: {}, eventId: {} and status: {}",
                controllerName, requesterId, eventId, status);
        return service.existsByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
    }
}
