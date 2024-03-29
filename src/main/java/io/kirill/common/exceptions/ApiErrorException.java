package io.kirill.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ApiErrorException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
}
