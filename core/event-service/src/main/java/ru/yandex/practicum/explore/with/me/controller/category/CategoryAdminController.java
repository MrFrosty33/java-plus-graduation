package ru.yandex.practicum.explore.with.me.controller.category;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.category.NewCategoryDto;
import ru.yandex.practicum.explore.with.me.service.category.CategoryServiceImpl;
import ru.yandex.practicum.interaction.api.model.category.dto.CategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryAdminController {
    private final String className = this.getClass().getSimpleName();
    private final CategoryServiceImpl categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto category) {
        log.trace("{}: createCategory() call with category: {}", className, category);
        return categoryService.createCategory(category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @NotNull @PositiveOrZero Long id) {
        log.trace("{}:  deleteCategory() call with id: {}", className, id);
        categoryService.deleteCategory(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable @NotNull @PositiveOrZero Long id,
                                      @RequestBody @Valid NewCategoryDto category) {
        log.trace("{}: updateCategory with id: {}", className, id);
        return categoryService.updateCategory(id, category);
    }
}