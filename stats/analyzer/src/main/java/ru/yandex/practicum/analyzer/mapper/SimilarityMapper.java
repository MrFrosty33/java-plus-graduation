package ru.yandex.practicum.analyzer.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.analyzer.model.Similarity;
import ru.yandex.practicum.analyzer.model.SimilarityDto;

@Mapper(componentModel = "spring")
public interface SimilarityMapper {
    SimilarityDto toDto(Similarity similarity);
}
