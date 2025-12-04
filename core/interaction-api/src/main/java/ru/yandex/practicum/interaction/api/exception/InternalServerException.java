package ru.yandex.practicum.interaction.api.exception;

public class InternalServerException extends CustomException {
    public InternalServerException(String reason, String message) {
        super(reason, message);
    }
}
