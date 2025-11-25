package ru.yandex.practicum.stat.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("""
        SELECT new ru.yandex.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(h))
        FROM Hit h
        WHERE h.created BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h) DESC
    """)
    List<ViewStats> findAllHits(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris);

    @Query("""
        SELECT new ru.yandex.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM Hit h
        WHERE h.created BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(DISTINCT h.ip) DESC
    """)
    List<ViewStats> findUniqueHits(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
}
