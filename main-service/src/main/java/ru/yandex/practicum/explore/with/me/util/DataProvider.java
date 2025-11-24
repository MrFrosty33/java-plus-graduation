package ru.yandex.practicum.explore.with.me.util;

public interface DataProvider<D, E> {
    D getDto(E entity);
}
