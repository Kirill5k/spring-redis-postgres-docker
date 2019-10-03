package io.kirill.customer.models;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class CreateCustomerRequest {

    @NotEmpty
    @Email
    private final String email;
}
