package ru.yandex.practicum.interaction.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ApiError {
    // как-то странно, в спецификации это поле есть в классе,
    // но не используется как таковое в теле ответа...
    @JsonIgnore
    private String errors;
    private HttpStatus status;
    private String reason;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
