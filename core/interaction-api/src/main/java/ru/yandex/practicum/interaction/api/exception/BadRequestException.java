package ru.yandex.practicum.interaction.api.exception;

public class BadRequestException extends CustomException {
    public BadRequestException(String reason, String message) {
        super(reason, message);
    }
}
