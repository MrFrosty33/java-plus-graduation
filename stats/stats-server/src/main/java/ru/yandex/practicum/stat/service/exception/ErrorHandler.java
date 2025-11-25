package ru.yandex.practicum.stat.service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@Generated
public class ErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        throw new StatsValidationException(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        throw new StatsValidationException(message);
    }

    @ExceptionHandler({InvalidFormatException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageConversionException.class,
            MissingServletRequestParameterException.class,
            ValueInstantiationException.class
    })
    public void handleIllegalArgumentException(Exception e) {
        throw new StatsValidationException(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(StatsValidationException e) {
        log.warn("400 Validation exception on Stat Service: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception e) {
        log.warn("500 on Stat Service {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }
}
