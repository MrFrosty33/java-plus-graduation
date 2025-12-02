package ru.yandex.practicum.explore.with.me.service.comment;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.explore.with.me.model.comment.CommentUpdateDto;
import ru.yandex.practicum.explore.with.me.model.comment.CommentUserDto;
import ru.yandex.practicum.explore.with.me.model.comment.CreateUpdateCommentDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    // admin

    // Получить комментарий по id (админ)
    CommentDto getCommentById(Long id);

    // Удалить комментарий админом
    void deleteCommentByAdmin(Long id);

    // private

    // Создать комментарий к событию от пользователя
    CommentDto createComment(Long userId, Long eventId, CreateUpdateCommentDto dto);

    // Обновить текст комментария автором
    CommentUpdateDto updateComment(Long userId, Long commentId, CreateUpdateCommentDto dto);

    // Удалить комментарий автором
    void deleteCommentByAuthor(Long userId, Long commentId);

    // Получить собственные комментарии пользователя
    List<CommentUserDto> getCommentsByAuthor(Long userId, Pageable pageable);

    // public

    // Публичный список комментариев события
    List<CommentDto> getCommentsByEvent(Long eventId, Pageable pageable);
}
