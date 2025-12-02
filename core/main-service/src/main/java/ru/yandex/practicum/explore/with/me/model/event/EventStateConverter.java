package ru.yandex.practicum.explore.with.me.model.event;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.yandex.practicum.interaction.api.model.event.EventState;

@Converter(autoApply = true)
public class EventStateConverter implements AttributeConverter<EventState, String> {
    @Override
    public String convertToDatabaseColumn(EventState status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public EventState convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EventState.valueOf(dbData.toUpperCase());
    }
}
