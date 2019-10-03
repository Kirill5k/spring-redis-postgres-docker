package io.kirill.customer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.kirill.customer.exceptions.CustomerNotFoundException;
import io.kirill.loan.Loan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CustomerController.class)
public class CustomerControllerTest {

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    private String requestJson = "{\"email\": \"%s\"}";
    private String email = "foo@bar.com";
    private long customerId = 1L;
    private Customer customer = new Customer(customerId, email, new ArrayList<>());
    private Loan loan = new Loan(customer, BigDecimal.TEN, 10, BigDecimal.ONE);

    @Test
    public void createCustomer() throws Exception {
        var requestBody = String.format(requestJson, email);

        doReturn(customer).when(customerService).create(email);

        mockMvc.perform(post("/api/customers")
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().json("{\"id\": 1}"));

        verify(customerService).create(email);
    }

    @Test
    public void createCustomerWhenNoEmail() throws Exception {
        var requestBody = String.format(requestJson, "");

        mockMvc.perform(post("/api/customers")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: must not be empty"));
    }

    @Test
    public void createCustomerWhenInvalidEmail() throws Exception {
        var requestBody = String.format(requestJson, "foo-bar");

        mockMvc.perform(post("/api/customers")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: must be a well-formed email address"));
    }

    @Test
    public void getCustomer() throws Exception {
        loan.setDateCreated(LocalDateTime.of(2019, 1, 1, 12, 0).toInstant(ZoneOffset.UTC));
        when(customerService.get(customerId)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(customerJson));

        verify(customerService).get(customerId);
    }

    @Test
    public void getCustomerWhenCustomerNotFound() throws Exception {
        doThrow(new CustomerNotFoundException(1L)).when(customerService).get(customerId);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("customer with id 1 does not exist"));
    }

    //language=JSON
    private String customerJson = "{\n" +
            "    \"email\": \"foo@bar.com\",\n" +
            "    \"loans\": [\n" +
            "        {\n" +
            "            \"loanAmount\": 10,\n" +
            "            \"remainingLoanAmount\": 10,\n" +
            "            \"term\": 10,\n" +
            "            \"weeklyPercentageRate\": 1,\n" +
            "            \"originalTerm\": 10,\n" +
            "            \"originalWeeklyPercentageRate\": 1,\n" +
            "            \"createdDate\": \"2019-01-01T12:00:00Z\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
}