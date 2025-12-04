package ru.yandex.practicum.interaction.api.util;

public interface DataProvider<D, E> {
    D getDto(E entity);
}
