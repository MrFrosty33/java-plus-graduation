package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.Interaction;

public interface InteractionRepository extends JpaRepository<Long, Interaction> {
}
