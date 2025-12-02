package ru.yandex.practicum.explore.with.me.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.explore.with.me.model.category.Category;
import ru.yandex.practicum.explore.with.me.model.category.NewCategoryDto;
import ru.yandex.practicum.interaction.api.model.category.dto.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toModel(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);
}