package ru.yandex.practicum.explore.with.me.model.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventViewsParameters {
    private LocalDateTime start;
    private LocalDateTime end;
    private List<Long> eventIds;
    private boolean unique;

    public List<String> getEventIdUris() {
        return eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();
    }
}
