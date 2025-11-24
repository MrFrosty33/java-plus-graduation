package ru.yandex.practicum.explore.with.me.exception;

public class ConflictException extends CustomException {
    public ConflictException(String reason, String message) {
        super(reason, message);
    }
}
