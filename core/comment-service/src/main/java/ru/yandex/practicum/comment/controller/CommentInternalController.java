package ru.yandex.practicum.comment.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.comment.service.CommentService;
import ru.yandex.practicum.interaction.api.feign.CommentClient;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentInternalController implements CommentClient {
    private final String className = this.getClass().getSimpleName();
    private final CommentService commentService;

    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getCommentsByEvent() call with eventId: {}, from: {}, size: {}",
                className, eventId, from, size);
        return commentService.getCommentsByEvent(eventId, PageRequest.of(from / size, size));
    }
}
