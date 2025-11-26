package ru.yandex.practicum.interaction.api.exception;

public class ConflictException extends CustomException {
    public ConflictException(String reason, String message) {
        super(reason, message);
    }
}
