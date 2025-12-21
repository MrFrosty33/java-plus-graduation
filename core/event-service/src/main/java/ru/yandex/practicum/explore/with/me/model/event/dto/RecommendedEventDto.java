package ru.yandex.practicum.explore.with.me.model.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedEventDto {
    private Long eventId;
    private Double score;
}
