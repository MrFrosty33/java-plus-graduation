package ru.yandex.practicum.explore.with.me.exception;

public class InternalServerException extends CustomException {
    public InternalServerException(String reason, String message) {
        super(reason, message);
    }
}
