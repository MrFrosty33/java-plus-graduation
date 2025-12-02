package ru.yandex.practicum.explore.with.me.service.event;

import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.model.event.EventStatistics;
import ru.yandex.practicum.explore.with.me.model.event.PublicEventParam;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventViewsParameters;
import ru.yandex.practicum.explore.with.me.model.event.dto.NewEventDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventFullDto createEvent(long userId, NewEventDto eventDto);

    EventFullDto getPrivateEventById(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto getPublicEventById(long eventId);

    List<EventShortDto> getEventsByUser(long userId, int from, int count);

    Map<Long, Long> getEventViews(EventViewsParameters params);

    List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                            EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getPublicEvents(PublicEventParam params);

    Map<Long, Integer> getConfirmedRequests(List<Long> eventIds);

    EventStatistics getEventStatistics(List<Event> events, LocalDateTime startStats, LocalDateTime endStats);
}
