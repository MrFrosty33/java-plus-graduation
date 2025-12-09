package ru.yandex.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.analyzer.model.Interaction;
import ru.yandex.practicum.analyzer.model.InteractionDto;

@Mapper(componentModel = "spring")
public interface InteractionMapper {
    InteractionDto toDto(Interaction interaction);
}
