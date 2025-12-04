package ru.yandex.practicum.interaction.api.util;

public interface ExistenceValidator<T> {
    void validateExists(Long id);
}