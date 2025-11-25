package ru.yandex.practicum.stat.service.exception;

public class StatsValidationException extends RuntimeException {
    public StatsValidationException(String message) {
        super(message);
    }
}
