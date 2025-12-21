package ru.yandex.practicum.aggregator.service;

import lombok.Data;

@Data
public class EventPair {
    private final Long eventA;
    private final Long eventB;
}
