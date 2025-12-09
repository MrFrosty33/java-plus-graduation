package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.Similarity;

public interface SimilarityRepository extends JpaRepository<Long, Similarity> {
}
