package io.kirill.customer.exceptions;

import io.kirill.common.exceptions.ApiErrorException;
import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends ApiErrorException {

    public CustomerNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, String.format("customer with id %d does not exist", id));
    }
}
