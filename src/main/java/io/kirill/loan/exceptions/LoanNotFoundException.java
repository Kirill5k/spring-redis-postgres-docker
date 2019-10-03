package io.kirill.loan.exceptions;

import io.kirill.common.exceptions.ApiErrorException;
import org.springframework.http.HttpStatus;

public class LoanNotFoundException extends ApiErrorException {

    public LoanNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, String.format("loan with id %d does not exist", id));
    }
}
