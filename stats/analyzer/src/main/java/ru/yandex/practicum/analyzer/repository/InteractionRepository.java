package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByUserId(Long userId);

    List<Interaction> findByEventIdIn(List<Long> eventId);

    List<Interaction> findByUserIdAndEventIdIn(Long userId, List<Long> eventId);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);
}
