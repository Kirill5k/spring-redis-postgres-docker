package io.kirill.loan.exceptions;

import io.kirill.common.exceptions.ApiErrorException;
import org.springframework.http.HttpStatus;

public class LoanExtensionException extends ApiErrorException {

    public LoanExtensionException(int currentTerm, int newTerm) {
        super(HttpStatus.BAD_REQUEST, String.format("new term (%d) must be greater than the current term (%d)", newTerm, currentTerm));
    }
}
