package ru.yandex.practicum.stat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.stats.dto.EndpointHitCreate;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void saveHit(EndpointHitCreate hitCreate) {
        Hit hit = Hit.builder()
                .app(hitCreate.getApp())
                .uri(hitCreate.getUri())
                .ip(hitCreate.getIp())
                .created(hitCreate.getTimestamp())
                .build();
         statsRepository.save(hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return statsRepository.findUniqueHits(start, end, uris);
        }
        return statsRepository.findAllHits(start, end, uris);
    }
}
