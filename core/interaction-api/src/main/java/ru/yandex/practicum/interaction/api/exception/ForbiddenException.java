package ru.yandex.practicum.interaction.api.exception;

public class ForbiddenException extends CustomException {
    public ForbiddenException(String reason, String message) {
        super(reason, message);
    }
}
