package ru.yandex.practicum.explore.with.me.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.explore.with.me.model.category.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name")
    boolean isExistName(@Param("name") String name);

    @Query("SELECT DISTINCT c FROM Category c")
    Page<Category> findAllDistinct(Pageable pageable);
}