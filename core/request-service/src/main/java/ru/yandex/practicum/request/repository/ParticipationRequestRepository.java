package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);


    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long requesterId,
                                                   Long eventId,
                                                   ParticipationRequestStatus status);

    int countByEventId(Long eventId);

    @Query("""
                SELECT new ru.yandex.practicum.interaction.api.model.event.dto.EventRequestCount(r.eventId, COUNT(r))
                    FROM ParticipationRequest r
                    WHERE r.eventId IN :eventIds
                    AND r.status = 'confirmed'
                    GROUP BY r.eventId
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
