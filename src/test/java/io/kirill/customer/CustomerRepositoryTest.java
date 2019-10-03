package io.kirill.customer;

import static org.assertj.core.api.Assertions.assertThat;

import io.kirill.loan.Loan;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private String email = "foo@bar.com";
    private BigDecimal loanAmount = BigDecimal.valueOf(100);
    private int term = 10;

    @Test
    public void save() {
        var result = customerRepository.save(new Customer(email));

        assertThat(result).isEqualTo(entityManager.find(Customer.class, result.getId()));
    }

    @Test
    public void existsById() {
        var customer = entityManager.persist(new Customer(email));

        assertThat(customerRepository.existsById(customer.getId())).isTrue();
        assertThat(customerRepository.existsById(99999L)).isFalse();
    }

    @Test
    public void findById() {
        var customer = entityManager.persistAndFlush(new Customer(email));
        var loan = entityManager.persistAndFlush(new Loan(customer, loanAmount, term, BigDecimal.valueOf(2.0)));

        var result = customerRepository.findById(customer.getId()).get();

        assertThat(result.getLoans()).containsExactly(loan);
        assertThat(result).isEqualTo(customer);
    }
}