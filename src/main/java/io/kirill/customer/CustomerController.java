package io.kirill.customer;

import static org.springframework.http.HttpStatus.CREATED;

import io.kirill.customer.models.CreateCustomerRequest;
import io.kirill.customer.models.CreateCustomerResponse;
import io.kirill.customer.models.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateCustomerResponse create(@Validated @RequestBody CreateCustomerRequest createCustomerRequest) {
        var customer = customerService.create(createCustomerRequest.getEmail());
        return new CreateCustomerResponse(customer.getId());
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        var customer = customerService.get(id);
        return new CustomerResponse(customer);
    }
}
