package ru.yandex.practicum.explore.with.me.model.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentUserDto {
    private long id;
    private String text;
    private CommentEventDto eventDto;
    private LocalDateTime createdOn;

    public static record CommentEventDto(long id, String title) {
    }
}
