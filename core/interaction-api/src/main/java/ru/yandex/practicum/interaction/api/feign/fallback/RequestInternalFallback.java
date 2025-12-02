package ru.yandex.practicum.interaction.api.feign.fallback;

import ru.yandex.practicum.interaction.api.feign.RequestInternalFeignClient;
import ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;

import java.util.List;

public class RequestInternalFallback implements RequestInternalFeignClient {

    @Override
    public List<ParticipationRequestDto> findAllById(List<Long> requestIds) {
        return List.of();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(long eventId) {
        return List.of();
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(long eventId, ParticipationRequestStatus status) {
        return List.of();
    }

    @Override
    public void updateStatus(List<Long> requestIds, ParticipationRequestStatus status) {

    }

    @Override
    public List<EventRequestCount> countGroupByEventId(List<Long> eventIds) {
        return List.of();
    }
}
