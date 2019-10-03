package io.kirill.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kirill.customer.exceptions.CustomerNotFoundException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private long customerId = 1L;
    private long invalidCustomerId = 2L;
    private String email = "foo@bar.com";
    private Customer customer = new Customer(email);

    @Before
    public void setUp() {
        doAnswer(invocation -> invocation.getArgument(0)).when(customerRepository).save(any());
    }

    @Test
    public void create() {
        var result = customerService.create(email);

        assertThat(result.getEmail()).isEqualTo(email);
        verify(customerRepository).save(result);
    }

    @Test
    public void get() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());

        var result = customerService.get(customerId);
        assertThat(result).isEqualTo(customer);

        assertThat(catchThrowable(() -> customerService.get(invalidCustomerId))).isInstanceOf(CustomerNotFoundException.class);

    }
}