package ru.yandex.practicum.explore.with.me.controller.category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.service.category.CategoryServiceImpl;
import ru.yandex.practicum.interaction.api.model.category.dto.CategoryDto;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryPublicController {
    private final String className = this.getClass().getSimpleName();
    private final CategoryServiceImpl categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getCategories() call with from: {}, size: {}", className, from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable("id") @PositiveOrZero @NotNull Long id) {
        log.trace("{}: getCategoryById() with id: {}", className, id);
        return categoryService.getCategory(id);
    }
}