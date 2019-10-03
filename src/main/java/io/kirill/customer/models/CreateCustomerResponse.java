package io.kirill.customer.models;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class CreateCustomerResponse {
    private final Long id;
}
