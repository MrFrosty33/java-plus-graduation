package ru.yandex.practicum.explore.with.me.model.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction.api.model.event.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "The length of annotation should be between 20 and 2000 symbols")
    private String annotation;

    @PositiveOrZero
    private Long category;

    @Size(min = 20, max = 7000, message = "The length of description should be between 20 and 7000 symbols")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;
    private Boolean paid;

    @PositiveOrZero(message = "The participant limit must be greater than or equal to zero.")
    private Integer participantLimit;
    private Boolean requestModeration;
    private UpdateEventUserAction stateAction;

    @Size(min = 3, max = 120, message = "The length of title should be between 3 and 120 symbols")
    private String title;

    @AssertTrue(message = "The event must start at least 2 hours from now.")
    public boolean isStartEventValid() {
        if (eventDate == null) {
            return true;
        }
        return eventDate.isAfter(LocalDateTime.now().plusHours(2));
    }
}
