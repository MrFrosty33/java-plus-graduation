package ru.yandex.practicum.explore.with.me.model.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.explore.with.me.model.event.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000, message = "The length of annotation should be between 20 and 2000 symbols")
    private String annotation;

    @NotNull
    @PositiveOrZero
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000, message = "The length of description should be between 20 and 7000 symbols")
    private String description;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private Location location;
    private boolean paid = false;

    @PositiveOrZero(message = "The participant limit must be greater than or equal to zero.")
    private int participantLimit = 0;
    private boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120, message = "The length of title should be between 3 and 120 symbols")
    private String title;

    @AssertTrue(message = "The event must start at least 2 hours from now.")
    public boolean isStartEventValid() {
        return eventDate.isAfter(LocalDateTime.now().plusHours(2));
    }
}
