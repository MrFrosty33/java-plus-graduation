package ru.yandex.practicum.explore.with.me.model.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentUpdateDto {
    private long id;
    private String text;
    private CommentAuthorDto authorDto;
    private CommentEventDto eventDto;
    private LocalDateTime updatedOn;

    public static record CommentAuthorDto(long id, String name) {
    }

    public static record CommentEventDto(long id, String title) {
    }
}
