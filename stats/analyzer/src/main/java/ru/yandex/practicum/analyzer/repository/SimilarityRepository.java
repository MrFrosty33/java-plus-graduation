package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    @Query("SELECT s FROM Similarity s WHERE s.eventIdA = :event OR s.eventIdB = :event")
    List<Similarity> findByEventIdAOrEventIdB(@Param("event") Long event);

    @Query("SELECT s FROM Similarity s WHERE s.eventIdA IN :eventIds OR s.eventIdB IN :eventIds")
    List<Similarity> findByEventIdAOrEventIdBIn(@Param("eventIds") List<Long> eventIds);

    Optional<Similarity> findByEventIdAAndEventIdB(Long eventIdA, Long eventIdB);

}
