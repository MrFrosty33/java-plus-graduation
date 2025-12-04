package ru.yandex.practicum.interaction.api.feign.fallback;

import ru.yandex.practicum.interaction.api.feign.CommentClient;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;

import java.util.List;

public class CommentFallback implements CommentClient {
    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        return List.of();
    }
}
