package ru.yandex.practicum.stats.client;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.stats.dto.EndpointHitCreate;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    ResponseEntity<Void> createHit(EndpointHitCreate endpointHitCreate);

    ResponseEntity<List<ViewStats>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

    //todo удаляем из stats всё, кроме stats-client
    //todo добавляем aggregator, collector, analyzer, serialization
}
