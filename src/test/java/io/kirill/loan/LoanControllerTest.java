package io.kirill.loan;


import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.kirill.customer.Customer;
import io.kirill.customer.CustomerService;
import io.kirill.customer.exceptions.CustomerNotFoundException;
import io.kirill.loan.exceptions.LoanApprovalException;
import io.kirill.loan.exceptions.LoanNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

    @MockBean
    private CustomerService customerService;

    @MockBean
    private LoanRiskAnalyst loanRiskAnalyst;

    @MockBean
    private LoanService loanService;

    @Autowired
    private MockMvc mockMvc;

    private String ipAddress = "192.168.0.1";
    private long customerId = 1L;
    private long loanId = 2L;
    private int term = 10;
    private BigDecimal loanAmount = BigDecimal.valueOf(55);
    private String requestJson = "{\"customerId\": %d, \"loanAmount\": %d, \"term\": %d}";;

    private Loan loan = Loan.builder().id(loanId).build();
    private Customer customer = new Customer(customerId, "foo@bar.com", emptyList());

    @Test
    public void apply() throws Exception {
        var requestBody = String.format(requestJson, customerId, loanAmount.intValue(), term);

        doReturn(customer).when(customerService).get(customerId);
        doReturn(loan).when(loanService).create(customer, loanAmount, term);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\": 2}"));

        verify(customerService).get(customerId);
        verify(loanRiskAnalyst).assess(eq(ipAddress), eq(loanAmount), any(LocalDateTime.class));
    }

    @Test
    public void applyWhenCustomerDoesNotExist() throws Exception {
        var requestBody = String.format(requestJson, customerId, loanAmount.intValue(), term);

        doThrow(new CustomerNotFoundException(customerId)).when(customerService).get(customerId);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("customer with id 1 does not exist"));
    }

    @Test
    public void applyWhenLoanNotApproved() throws Exception {
        var requestBody = String.format(requestJson, customerId, loanAmount.intValue(), term);

        doThrow(new LoanApprovalException(ipAddress, 3, 24)).when(loanRiskAnalyst).assess(any(), any(), any());

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("denied. 3 attempts within 24 hours from 192.168.0.1"));
    }

    @Test
    public void applyWhenNoCustomerId() throws Exception {
        var requestBody = String.format(requestJson, null, loanAmount.intValue(), term);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("customerId: must not be null"));
    }

    @Test
    public void applyWhenNoLoanAmount() throws Exception {
        var requestBody = String.format(requestJson, customerId, null, term);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("loanAmount: must not be null"));
    }

    @Test
    public void applyWhenSmallLoanAmount() throws Exception {
        var requestBody = String.format(requestJson, customerId, 10, term);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("loanAmount: must be greater than or equal to 50"));
    }

    @Test
    public void applyWhenBigLoanAmount() throws Exception {
        var requestBody = String.format(requestJson, customerId, 600, term);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("loanAmount: must be less than or equal to 500"));
    }

    @Test
    public void applyWhenNoTerm() throws Exception {
        var requestBody = String.format(requestJson, customerId, loanAmount.intValue(), null);

        mockMvc.perform(post("/api/loans")
                .content(requestBody)
                .header("X-FORWARDED-FOR", ipAddress)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("term: must not be null"));
    }

    @Test
    public void extend() throws Exception {
        mockMvc.perform(put("/api/loans/2/term")
                .content(String.format("{\"term\": %d}", term))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(loanService).extend(loanId, term);
    }

    @Test
    public void extendWhenLoanNotFound() throws Exception {
        doThrow(new LoanNotFoundException(loanId)).when(loanService).extend(loanId, term);

        mockMvc.perform(put("/api/loans/2/term")
                .content(String.format("{\"term\": %d}", term))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("loan with id 2 does not exist"));

        verify(loanService).extend(loanId, term);
    }

    @Test
    public void extendWhenSmallTerm() throws Exception {
        doThrow(new LoanNotFoundException(loanId)).when(loanService).extend(loanId, term);

        mockMvc.perform(put("/api/loans/2/term")
                .content(String.format("{\"term\": %d}", 1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("term: must be greater than or equal to 2"));
    }

    @Test
    public void extendWhenBigTerm() throws Exception {
        doThrow(new LoanNotFoundException(loanId)).when(loanService).extend(loanId, term);

        mockMvc.perform(put("/api/loans/2/term")
                .content(String.format("{\"term\": %d}", 1000))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("term: must be less than or equal to 52"));
    }
}