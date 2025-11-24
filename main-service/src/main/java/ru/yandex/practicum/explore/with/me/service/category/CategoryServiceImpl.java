package ru.yandex.practicum.explore.with.me.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.exception.ConflictException;
import ru.yandex.practicum.explore.with.me.exception.NotFoundException;
import ru.yandex.practicum.explore.with.me.mapper.CategoryMapper;
import ru.yandex.practicum.explore.with.me.model.category.Category;
import ru.yandex.practicum.explore.with.me.model.category.CategoryDto;
import ru.yandex.practicum.explore.with.me.model.category.NewCategoryDto;
import ru.yandex.practicum.explore.with.me.repository.CategoryRepository;
import ru.yandex.practicum.explore.with.me.util.DataProvider;
import ru.yandex.practicum.explore.with.me.util.ExistenceValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ExistenceValidator<Category>,
        CategoryService, DataProvider<CategoryDto, Category> {
    private final String className = this.getClass().getSimpleName();
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void validateExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            log.info("{}: attempt to find category with id: {}", className, id);
            throw new NotFoundException("The required object was not found.",
                    "Category with id=" + id + " was not found");
        }
    }

    @Override
    public CategoryDto getDto(Category entity) {
        return categoryMapper.toDto(entity);
    }

    private void validateNameUnique(String categoryName) {
        if (categoryRepository.isExistName(categoryName)) {
            log.info("{}: attempt to create / update category with already reserved name:{}", className, categoryName);
            throw new ConflictException("The name of category should be unique.",
                    "Category with name=" + categoryName + " is already exist");
        }
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        validateNameUnique(categoryDto.getName());
        Category category = categoryRepository.save(categoryMapper.toModel(categoryDto));
        CategoryDto result = categoryMapper.toDto(category);
        log.info("{}: result of createCategory(): {}", className, result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        Category category = findCategoryByIdOrElseThrow(id);
        if (!category.getEvents().isEmpty()) {
            log.info("{}: attempt to delete category: {}, which is not empty", className, category);
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "The category is not empty");
        }
        categoryRepository.deleteById(id);
        log.info("{}: category with id: {} was deleted", className, id);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long id, NewCategoryDto categoryDto) {
        Category categoryToUpdate = findCategoryByIdOrElseThrow(id);

        if (categoryToUpdate.getName().equals(categoryDto.getName())) {
            return categoryMapper.toDto(categoryToUpdate);
        }

        validateNameUnique(categoryDto.getName());
        categoryToUpdate.setName(categoryDto.getName());
        Category category = categoryRepository.save(categoryToUpdate);
        CategoryDto result = categoryMapper.toDto(category);
        log.info("{}: result of updateCategory(): {}", className, result);
        return result;
    }

    @Override
    public CategoryDto getCategory(long id) {
        Category category = findCategoryByIdOrElseThrow(id);
        CategoryDto result = categoryMapper.toDto(category);
        log.info("{}: result of getCategory(): {}", className, result);
        return result;
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
        List<CategoryDto> result = categoryRepository.findAllDistinct(pageable).getContent()
                .stream().map(categoryMapper::toDto).toList();
        log.info("{}: result of getCategories(): {}", className, result);
        return result;
    }

    private Category findCategoryByIdOrElseThrow(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> {
            log.info("{}: category with id: {} was not found", className, categoryId);
            return new NotFoundException("The required object was not found.", "Category with id=" + categoryId + " was not found");
        });
    }
}