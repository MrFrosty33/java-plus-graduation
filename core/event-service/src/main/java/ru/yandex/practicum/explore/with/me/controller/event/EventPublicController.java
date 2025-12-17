package ru.yandex.practicum.explore.with.me.controller.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.explore.with.me.model.event.EventPublicSort;
import ru.yandex.practicum.explore.with.me.model.event.PublicEventParam;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.RecommendedEventDto;
import ru.yandex.practicum.explore.with.me.service.event.EventService;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;
import ru.yandex.practicum.stats.client.CollectorClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPublicController {
    private final EventService eventsService;
    private final CollectorClient collectorClient;
    private final String className = this.getClass().getSimpleName();

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) EventPublicSort sort,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        PublicEventParam publicEventParam = new PublicEventParam();
        publicEventParam.setText(Objects.requireNonNullElse(text, ""));
        publicEventParam.setCategories(categories);
        publicEventParam.setPaid(paid);
        publicEventParam.setRangeStart(rangeStart);
        publicEventParam.setRangeEnd(rangeEnd);
        publicEventParam.setOnlyAvailable(onlyAvailable);
        publicEventParam.setSort(sort);
        publicEventParam.setFrom(from);
        publicEventParam.setSize(size);
        log.trace("{}: getEvents() call with publicEventParam: {}", className, publicEventParam);

        return eventsService.getPublicEvents(publicEventParam);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                     @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.trace("{}: getEventById() call with eventId: {} and userId: {}", className, eventId, userId);
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .build();
        collectorClient.collectUserAction(userActionProto);

        return eventsService.getPublicEventById(eventId);
    }

    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getCommentsByEvent() call with eventId: {}, from: {}, size: {}",
                className, eventId, from, size);
        return eventsService.getCommentsByEvent(eventId, from, size);
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<RecommendedEventDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId, @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getRecommendations() call with userId: {} and size: {}",
                className, userId, size);
        return eventsService.getRecommendations(userId, size);
    }
}
