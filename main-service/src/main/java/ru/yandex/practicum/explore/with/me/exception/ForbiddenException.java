package ru.yandex.practicum.explore.with.me.exception;

public class ForbiddenException extends CustomException {
    public ForbiddenException(String reason, String message) {
        super(reason, message);
    }
}
