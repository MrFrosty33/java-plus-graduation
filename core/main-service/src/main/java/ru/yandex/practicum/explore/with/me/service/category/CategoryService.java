package ru.yandex.practicum.explore.with.me.service.category;

import ru.yandex.practicum.explore.with.me.model.category.CategoryDto;
import ru.yandex.practicum.explore.with.me.model.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto categoryDto);

    void deleteCategory(long id);

    CategoryDto updateCategory(long id, NewCategoryDto categoryDto);

    CategoryDto getCategory(long id);

    List<CategoryDto> getCategories(int from, int size);
}
