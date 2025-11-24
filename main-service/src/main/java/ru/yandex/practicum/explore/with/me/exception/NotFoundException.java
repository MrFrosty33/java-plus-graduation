package ru.yandex.practicum.explore.with.me.exception;

public class NotFoundException extends CustomException {
    public NotFoundException(String reason, String message) {
        super(reason, message);
    }
}
