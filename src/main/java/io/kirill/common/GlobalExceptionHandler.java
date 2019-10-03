package io.kirill.common;

import static java.util.stream.Collectors.joining;

import io.kirill.common.exceptions.ApiErrorException;
import io.kirill.common.models.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleBadRequest(MethodArgumentNotValidException exception) {
        log.error("error validating a request {}", exception.getMessage(), exception);
        String message = exception.getBindingResult().getAllErrors()
                .stream()
                .map(error -> String.format("%s: %s", getFieldName(error), error.getDefaultMessage()))
                .collect(joining(", "));
        return new ApiErrorResponse(message);
    }

    private String getFieldName(ObjectError error) {
        return error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.error("error http message not readable: {}, {}", exception.getMessage());
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleApiErrorException(ApiErrorException exception) {
        log.error("api error {} {}", exception.getHttpStatus(), exception.getMessage(), exception);
        return ResponseEntity.status(exception.getHttpStatus()).body(new ApiErrorResponse(exception.getMessage()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleGenericException(Exception exception) {
        log.error("unexpected error: {}", exception.getMessage(), exception);
        return new ApiErrorResponse(exception.getMessage());
    }
}

