package ru.yandex.practicum.stat.client;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.stat.dto.EndpointHitCreate;
import ru.yandex.practicum.stat.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatClient {
    ResponseEntity<Void> createHit(EndpointHitCreate endpointHitCreate);

    ResponseEntity<List<ViewStats>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
