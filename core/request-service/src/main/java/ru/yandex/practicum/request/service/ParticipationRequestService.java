package ru.yandex.practicum.request.service;

import ru.yandex.practicum.interaction.api.model.request.CancelParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.NewParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> find(Long userId);

    ParticipationRequestDto create(NewParticipationRequest newParticipationRequest);

    ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest);

    boolean isParticipantApproved(Long userId, Long eventId);
}
