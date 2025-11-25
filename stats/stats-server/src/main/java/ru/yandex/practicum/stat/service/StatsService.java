package ru.yandex.practicum.stat.service;

import ru.yandex.practicum.stats.dto.EndpointHitCreate;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(EndpointHitCreate hitCreate);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
