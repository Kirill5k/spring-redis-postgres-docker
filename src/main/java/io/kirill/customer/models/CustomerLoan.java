package io.kirill.customer.models;

import io.kirill.loan.Loan;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
class CustomerLoan {

    CustomerLoan(Loan loan) {
        this.loanAmount = loan.getLoanAmount();
        this.remainingLoanAmount = loan.getRemainingLoanAmount();
        this.term = loan.getTerm();
        this.weeklyPercentageRate = loan.getWeeklyPercentageRate();
        this.originalTerm = loan.getOriginalTerm();
        this.originalWeeklyPercentageRate = loan.getWeeklyPercentageRate();
        this.createdDate = loan.getDateCreated();
    }

    private final BigDecimal loanAmount;
    private final BigDecimal remainingLoanAmount;
    private final Integer term;
    private final BigDecimal weeklyPercentageRate;
    private final Integer originalTerm;
    private final BigDecimal originalWeeklyPercentageRate;
    private final Instant createdDate;
}
