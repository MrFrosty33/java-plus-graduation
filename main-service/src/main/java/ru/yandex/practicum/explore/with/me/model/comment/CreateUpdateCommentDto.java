package ru.yandex.practicum.explore.with.me.model.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUpdateCommentDto {
    @NotBlank(message = "Text must not be blank")
    @Size(max = 1000, message = "Text size must be less than 1000 characters")
    private String text;
}
