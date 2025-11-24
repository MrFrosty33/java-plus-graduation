package ru.yandex.practicum.explore.with.me.service.participation.request;

import ru.yandex.practicum.explore.with.me.model.participation.CancelParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.NewParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> find(Long userId);

    ParticipationRequestDto create(NewParticipationRequest newParticipationRequest);

    ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest);

    boolean isParticipantApproved(Long userId, Long eventId);
}
