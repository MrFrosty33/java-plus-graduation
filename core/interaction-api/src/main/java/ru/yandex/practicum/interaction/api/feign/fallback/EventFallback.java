package ru.yandex.practicum.interaction.api.feign.fallback;

import ru.yandex.practicum.interaction.api.feign.EventClient;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;

public class EventFallback implements EventClient {
    @Override
    public EventFullDto getEventById(Long eventId) {
        return null;
    }
}
