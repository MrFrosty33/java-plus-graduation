package ru.yandex.practicum.interaction.api.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CancelParticipationRequest {
    private Long userId;
    private Long requestId;
}
