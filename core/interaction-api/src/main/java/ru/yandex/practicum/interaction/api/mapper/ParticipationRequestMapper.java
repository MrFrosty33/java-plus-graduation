package ru.yandex.practicum.interaction.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    ParticipationRequestDto toDto(ParticipationRequest entity);
}
