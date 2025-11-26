package ru.yandex.practicum.interaction.api.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String reason;

    public CustomException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
