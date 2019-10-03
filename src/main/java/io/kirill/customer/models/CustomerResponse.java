package io.kirill.customer.models;

import static java.util.stream.Collectors.toList;

import io.kirill.customer.Customer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class CustomerResponse {

    public CustomerResponse(Customer customer) {
        email = customer.getEmail();
        loans = customer.getLoans().stream().map(CustomerLoan::new).collect(toList());
    }

    private final String email;
    private final List<CustomerLoan> loans;
}
