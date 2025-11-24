package ru.yandex.practicum.explore.with.me.exception;

public class StatsServerUnavailable extends CustomException {
    public StatsServerUnavailable(String reason, String message) {
        super(reason, message);
    }
}
