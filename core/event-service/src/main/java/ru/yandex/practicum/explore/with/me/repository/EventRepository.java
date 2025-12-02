package ru.yandex.practicum.explore.with.me.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.interaction.api.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("""
            SELECT DISTINCT e FROM Event e
            WHERE e.initiatorId = :userId
            """)
    Page<Event> findEventsByUser(@Param("userId") Long userId,
                                 Pageable pageable);

    Optional<Event> findByIdAndState(Long id, EventState state);

    @Query("""
            SELECT e FROM Event AS e
            WHERE e.state = 'published'
              AND (:text IS NULL
                OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%')))
              AND (:categories IS NULL OR e.category.id IN :categories)
              AND (:paid IS NULL OR e.paid = :paid)
              AND (CAST(:rangeStart AS DATE) IS NULL OR e.eventDate >= :rangeStart)
              AND (CAST(:rangeEnd AS DATE) IS NULL OR e.eventDate <= :rangeEnd)
              AND (
                CAST(:rangeStart AS DATE) IS NOT NULL OR CAST(:rangeEnd AS DATE) IS NOT NULL OR e.eventDate > CURRENT_TIMESTAMP
                OR CAST(:rangeStart AS DATE) IS NULL)
            """)
    Page<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    @Query("""
             SELECT e
             FROM Event e
             WHERE (:users      IS NULL OR e.initiator.id IN :users)
               AND (:states     IS NULL OR e.state IN :states)
               AND (:categories IS NULL OR e.category.id IN :categories)
               AND (e.eventDate >= COALESCE(:rangeStart, e.eventDate))
               AND (e.eventDate <= COALESCE(:rangeEnd, e.eventDate))
            """)
    Page<Event> searchForAdmin(@Param("users") List<Long> users,
                               @Param("states") List<EventState> states,
                               @Param("categories") List<Long> categories,
                               @Param("rangeStart") LocalDateTime rangeStart,
                               @Param("rangeEnd") LocalDateTime rangeEnd,
                               Pageable pageable);
}
