package ru.yandex.practicum.explore.with.me.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class EventStatistics {
    private final Map<Long, Double> interactions;
    private final Map<Long, Integer> confirmedRequests;

    public double getInteractions(Long eventId) {
        return interactions.getOrDefault(eventId, 0.0);
    }

    public int getConfirmedRequests(Long eventId) {
        return confirmedRequests.getOrDefault(eventId, 0);
    }

}
