package io.kirill.loan;

import static org.assertj.core.api.Assertions.assertThat;

import io.kirill.customer.Customer;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    private String email = "foo@bar.com";
    private BigDecimal loanAmount = BigDecimal.valueOf(100);
    private int term = 10;

    @Test
    public void save() {
        var customer = entityManager.persist(new Customer(email));

        var result = loanRepository.save(new Loan(customer, loanAmount, term, BigDecimal.valueOf(2.0)));

        assertThat(result).isEqualTo(entityManager.find(Loan.class, result.getId()));
    }

    @Test
    public void findById() {
        var customer = entityManager.persist(new Customer(email));
        var loan = entityManager.persist(new Loan(customer, loanAmount, term, BigDecimal.valueOf(2.0)));

        assertThat(loanRepository.findById(loan.getId())).contains(loan);
    }
}