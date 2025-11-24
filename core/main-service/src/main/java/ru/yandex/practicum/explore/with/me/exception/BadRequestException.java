package ru.yandex.practicum.explore.with.me.exception;

public class BadRequestException extends CustomException {
    public BadRequestException(String reason, String message) {
        super(reason, message);
    }
}
