package ru.yandex.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.comment.service.CommentService;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUpdateDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUserDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CreateUpdateCommentDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentPrivateController {
    private final String className = this.getClass().getSimpleName();
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                                    @RequestParam @NotNull @PositiveOrZero Long eventId,
                                    @RequestBody @Valid CreateUpdateCommentDto commentDto) {
        log.trace("{}: createComment() call with userId: {}, eventId: {}, commentDto: {}",
                className, userId, eventId, commentDto);
        return commentService.createComment(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentUpdateDto updateComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                                          @PathVariable @NotNull @PositiveOrZero Long commentId,
                                          @RequestBody @Valid CreateUpdateCommentDto commentDto) {
        log.info("Update comment {} for event {} by user {}", commentDto, commentId, userId);
        return commentService.updateComment(userId, commentId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NotNull @PositiveOrZero Long userId,
                              @PathVariable @NotNull @PositiveOrZero Long commentId) {
        log.trace("{}:  deleteComment() call with userId: {}, commentId: {}", className, userId, commentId);
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentUserDto> getCommentsByUser(@PathVariable @NotNull @PositiveOrZero Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.trace("{}: getCommentsByUser() call with userId: {}, from: {}, size: {}", className, userId, from, size);
        return commentService.getCommentsByAuthor(
                userId,
                PageRequest.of(from / size, size)
        );
    }
}
