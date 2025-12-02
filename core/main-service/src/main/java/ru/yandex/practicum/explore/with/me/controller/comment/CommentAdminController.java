package ru.yandex.practicum.explore.with.me.controller.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.service.comment.CommentService;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentAdminController {
    private final String className = this.getClass().getSimpleName();
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable @NotNull @PositiveOrZero Long commentId) {
        log.trace("{}: getCommentById() call with commentId: {}", className, commentId);
        return commentService.getCommentById(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentById(@PathVariable @NotNull @PositiveOrZero Long commentId) {
        log.trace("{}: deleteCommentById() call with commentId: {}", className, commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}
