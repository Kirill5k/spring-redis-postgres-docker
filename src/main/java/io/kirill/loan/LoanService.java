package io.kirill.loan;

import io.kirill.customer.Customer;
import io.kirill.loan.exceptions.LoanExtensionException;
import io.kirill.loan.exceptions.LoanNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanService {
    private static final BigDecimal STANDARD_WEEKLY_PERCENTAGE_RATE = BigDecimal.valueOf(2);
    private static final BigDecimal WEEKLY_INTEREST_FACTOR_MULTIPLIER = BigDecimal.valueOf(1.5);

    private final LoanRepository loanRepository;

    public Loan create(Customer customer, BigDecimal loanAmount, int term) {
        var loan = new Loan(customer, loanAmount, term, STANDARD_WEEKLY_PERCENTAGE_RATE);
        return loanRepository.save(loan);
    }

    public Loan extend(Long id, Integer newTerm) {
        var loan = loanRepository.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
        if (loan.getTerm() >= newTerm) {
            throw new LoanExtensionException(loan.getTerm(), newTerm);
        }
        loan.setTerm(newTerm);
        loan.setWeeklyPercentageRate(loan.getWeeklyPercentageRate().multiply(WEEKLY_INTEREST_FACTOR_MULTIPLIER));
        return loanRepository.save(loan);
    }
}
