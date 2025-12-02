package ru.yandex.practicum.comment.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.interaction.api.exception.ApiError;
import ru.yandex.practicum.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.interaction.api.exception.ConflictException;
import ru.yandex.practicum.interaction.api.exception.CustomException;
import ru.yandex.practicum.interaction.api.exception.ForbiddenException;
import ru.yandex.practicum.interaction.api.exception.InternalServerException;
import ru.yandex.practicum.interaction.api.exception.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({MissingServletRequestParameterException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(BadRequestException e) {
        writeLog(e);
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        writeLog(e);
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        writeLog(e);
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenException e) {
        writeLog(e);
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.FORBIDDEN)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServer(InternalServerException e) {
        writeLog(e);
        return ApiError.builder()
                .reason(e.getReason())
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String reason = "Incorrectly made request.";
        String message = String.format(
                "Failed to convert value of type '%s' to required type '%s'; %s",
                e.getValue() != null ? e.getValue().getClass().getSimpleName() : "(no value received)",
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "(no required type received)",
                e.getCause() != null ? e.getCause().getMessage() : "(no cause received)"
        );
        writeLog(e, reason, message);

        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String reason = "Incorrectly made request.";
        String message = "Validation error. ";

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            message = message.concat(error.getField() + ": " + error.getDefaultMessage() + ". ");
        }

        writeLog(e, reason, message);
        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        String reason = "Integrity constraint has been violated.";
        String message = String.format(
                "could not execute statement; SQL ['%s']; constraint ['%s']; nested exception is '%s'",
                e.getSQLState() != null ? e.getSQLState() + "; " : "[n/a]; ",
                e.getConstraintName() != null ? e.getConstraintName() + "; " : "[n/a]; ",
                e.getCause() != null ? e.getCause().toString() : "(no cause received)");
        writeLog(e, reason, message);

        return ApiError.builder()
                .reason(reason)
                .message(message)
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOthers(Exception e) {
        String reason = "Internal Server Error";
        writeLog(e, reason, e.getMessage());
        return ApiError.builder()
                .reason(reason)
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void writeLog(CustomException e) {
        log.warn("ErrorHandler: caught {} with reason: {} and message: {}",
                e.getClass().getSimpleName(), e.getReason(), e.getMessage());
    }

    private void writeLog(Exception e, String reason, String message) {
        log.warn("ErrorHandler: caught {} with reason: {} and message: {}",
                e.getClass(), reason, message);
    }
}
