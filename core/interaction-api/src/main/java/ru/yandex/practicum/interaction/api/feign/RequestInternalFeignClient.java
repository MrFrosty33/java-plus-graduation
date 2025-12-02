package ru.yandex.practicum.interaction.api.feign;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;

import java.util.List;

@FeignClient(name = "request-service", fallback = RequestInternalFeignClient.class)
public interface RequestInternalFeignClient {
    @GetMapping(path = "/internal/requests", params = "requestIds")
    List<ParticipationRequestDto> findAllById(@RequestParam
                                              @NotNull
                                              @NotEmpty
                                              List<Long> requestIds);

    @GetMapping(path = "/internal/requests", params = "eventId")
    List<ParticipationRequestDto> findAllByEventId(@RequestParam @Positive long eventId);

    @GetMapping(path = "/internal/requests", params = {"eventId", "status"})
    List<ParticipationRequestDto> findAllByEventIdAndStatus(@RequestParam @Positive long eventId,
                                                            @RequestParam ParticipationRequestStatus status);

    @PostMapping("/internal/requests/status/update")
    void updateStatus(@RequestParam
                      @NotNull
                      @NotEmpty
                      List<Long> requestIds,
                      ParticipationRequestStatus status);

    @GetMapping("/internal/requests/count")
    List<EventRequestCount> countGroupByEventId(@RequestParam
                                                @NotNull
                                                @NotEmpty
                                                List<Long> eventIds);

    @GetMapping("/internal/requests/exists")
    boolean existsByRequesterIdAndEventIdAndStatus(@RequestParam @NotNull @Positive Long requesterId,
                                                   @RequestParam @NotNull @Positive Long eventId,
                                                   ParticipationRequestStatus status);
}
