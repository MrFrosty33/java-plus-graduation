package ru.yandex.practicum.request.service;

import ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount;
import ru.yandex.practicum.interaction.api.model.request.CancelParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.NewParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> find(Long userId);

    ParticipationRequestDto create(NewParticipationRequest newParticipationRequest);

    ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest);

    boolean isParticipantApproved(Long userId, Long eventId);

    List<ParticipationRequestDto> findAllByEventId(long eventId);

    List<ParticipationRequestDto> findAllById(List<Long> requestIds);

    List<ParticipationRequestDto> findAllByEventIdAndStatus(long eventId, ParticipationRequestStatus status);

    void updateStatus(List<Long> requestIds, ParticipationRequestStatus status);

    List<EventRequestCount> countGroupByEventId(List<Long> eventIds);
}
