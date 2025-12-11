package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.Interaction;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Long, Interaction> {
    List<Interaction> findByUserId(Long userId);

    List<Interaction> findByEventIdIn(List<Long> eventId);
}
