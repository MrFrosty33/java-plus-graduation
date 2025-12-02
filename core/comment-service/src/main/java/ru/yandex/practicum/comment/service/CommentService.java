package ru.yandex.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUpdateDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUserDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CreateUpdateCommentDto;

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
