package io.kirill.loan;

import io.kirill.customer.Customer;
import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "loan")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    public Loan(Customer customer, BigDecimal loanAmount, Integer term, BigDecimal weeklyPercentageRate) {
        this.customer = customer;
        customer.getLoans().add(this);
        this.loanAmount = loanAmount;
        remainingLoanAmount = loanAmount;
        this.term = term;
        originalTerm = term;
        this.weeklyPercentageRate = weeklyPercentageRate;
        originalWeeklyPercentageRate = weeklyPercentageRate;
        dateCreated = Instant.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "remaining_loan_amount", nullable = false)
    private BigDecimal remainingLoanAmount;

    @Column(name = "term", nullable = false)
    private Integer term;

    @Column(name = "weekly_percentage_rate", nullable = false)
    private BigDecimal weeklyPercentageRate;

    @Column(name = "original_term", nullable = false)
    private Integer originalTerm;

    @Column(name = "original_weekly_percentage_rate", nullable = false)
    private BigDecimal originalWeeklyPercentageRate;

    @Column(name = "date_created", nullable = false)
    private Instant dateCreated;
}
