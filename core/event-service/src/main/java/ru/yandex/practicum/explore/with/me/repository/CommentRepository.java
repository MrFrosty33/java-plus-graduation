package ru.yandex.practicum.explore.with.me.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.explore.with.me.model.comment.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdOrderByCreatedOnDesc(Long eventId, Pageable pageable);

    List<Comment> findByAuthorIdOrderByCreatedOnDesc(Long authorId, Pageable pageable);
}
