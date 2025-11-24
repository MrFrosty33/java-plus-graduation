package ru.yandex.practicum.explore.with.me.model.participation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CancelParticipationRequest {
    private Long userId;
    private Long requestId;
}
