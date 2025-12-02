package ru.yandex.practicum.interaction.api.feign;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.interaction.api.feign.fallback.CommentFallback;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;

import java.util.List;

@FeignClient(name = "comment-service", fallback = CommentFallback.class)
public interface CommentClient {

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    List<CommentDto> getCommentsByEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size);
}
