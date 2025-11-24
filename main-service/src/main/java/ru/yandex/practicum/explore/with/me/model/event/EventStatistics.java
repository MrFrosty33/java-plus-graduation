package ru.yandex.practicum.explore.with.me.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class EventStatistics {
    private final Map<Long, Long> views;
    private final Map<Long, Integer> confirmedRequests;

    public long getViews(Long eventId) {
        return views.getOrDefault(eventId, 0L);
    }

    public int getConfirmedRequests(Long eventId) {
        return confirmedRequests.getOrDefault(eventId, 0);
    }

}
