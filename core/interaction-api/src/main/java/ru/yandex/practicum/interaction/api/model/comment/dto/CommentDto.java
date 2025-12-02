package ru.yandex.practicum.interaction.api.model.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;
    private String text;
    private CommentAuthorDto authorDto;
    private CommentEventDto eventDto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    public static record CommentAuthorDto(long id, String name) {
    }

    public static record CommentEventDto(long id, String title) {
    }
}
