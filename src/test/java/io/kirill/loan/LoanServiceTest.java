package io.kirill.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kirill.customer.Customer;
import io.kirill.loan.exceptions.LoanExtensionException;
import io.kirill.loan.exceptions.LoanNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoanServiceTest {
    private static final BigDecimal STANDARD_WEEKLY_PERCENTAGE_RATE = BigDecimal.valueOf(2);

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private long loanId = 1L;
    private long customerId = 2L;
    private BigDecimal loanAmount = BigDecimal.valueOf(100);
    private int term = 10;
    private Customer customer = new Customer(customerId, "foo@bar.com", new ArrayList<>());
    private Loan loan = new Loan(customer, loanAmount, term, STANDARD_WEEKLY_PERCENTAGE_RATE);

    @Before
    public void setUp() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(loanRepository).save(any());
    }

    @Test
    public void create() {
        var result = loanService.create(customer, loanAmount, term);

        assertThat(result).extracting(Loan::getOriginalTerm, Loan::getTerm).contains(term);
        assertThat(result).extracting(Loan::getRemainingLoanAmount, Loan::getLoanAmount).contains(loanAmount);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result).extracting(Loan::getOriginalWeeklyPercentageRate, Loan::getWeeklyPercentageRate).contains(STANDARD_WEEKLY_PERCENTAGE_RATE);
        verify(loanRepository).save(result);
    }

    @Test
    public void extend() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        var result = loanService.extend(loanId, 20);

        assertThat(result.getOriginalTerm()).isEqualTo(term);
        assertThat(result.getTerm()).isEqualTo(20);
        assertThat(result.getOriginalWeeklyPercentageRate()).isEqualTo(STANDARD_WEEKLY_PERCENTAGE_RATE);
        assertThat(result.getWeeklyPercentageRate()).isEqualTo(BigDecimal.valueOf(3.0));

        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(result);
    }

    @Test
    public void extendWhenLoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThat(catchThrowable(() -> loanService.extend(loanId, 20))).isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    public void extendWhenNewTermIsLessThanPrevious() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThat(catchThrowable(() -> loanService.extend(loanId, 5)))
                .isInstanceOf(LoanExtensionException.class)
                .hasMessage("new term (5) must be greater than the current term (10)");
    }
}