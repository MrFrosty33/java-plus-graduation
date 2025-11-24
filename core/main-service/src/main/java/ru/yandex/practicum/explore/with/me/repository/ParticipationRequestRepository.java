package ru.yandex.practicum.explore.with.me.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestCount;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);


    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long requesterId,
                                                   Long eventId,
                                                   ParticipationRequestStatus status);

    int countByEventId(Long eventId);

    @Query("""
                SELECT new ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestCount(r.event.id, COUNT(r))
                    FROM ParticipationRequest r
                    WHERE r.event.id IN :eventIds
                    AND r.status = 'confirmed'
                    GROUP BY r.event.id
            """)
    List<EventRequestCount> countGroupByEventId(@Param("eventIds") List<Long> eventIds);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    @Modifying
    @Query("""
                UPDATE ParticipationRequest pr
                SET pr.status = :status
                WHERE pr.id IN :requestIds
            """)
    void updateStatus(@Param("requestIds") List<Long> requestIds, @Param("status") ParticipationRequestStatus status);

}
